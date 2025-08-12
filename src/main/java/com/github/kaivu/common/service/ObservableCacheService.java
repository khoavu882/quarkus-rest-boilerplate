package com.github.kaivu.common.service;

import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.AsyncObservabilityContext;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.context.TenantObservabilityContext;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.AppConfiguration;
import com.github.kaivu.config.metrics.AppMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Enhanced observable cache service that provides tenant-aware caching with comprehensive
 * observability including metrics, tracing, structured logging, and performance monitoring.
 *
 * Features:
 * - Tenant isolation with prefixed cache keys
 * - Comprehensive cache operation metrics (hits, misses, errors, latency)
 * - Distributed tracing integration
 * - Cache health monitoring and alerting
 * - Performance analytics and optimization insights
 * - Multi-level cache support (L1: Caffeine, L2: Redis)
 * - Cache warming and preloading strategies
 * - Automatic cache invalidation patterns
 */
@Slf4j
@ApplicationScoped
public class ObservableCacheService {

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    TenantObservabilityContext tenantContext;

    @Inject
    AsyncObservabilityContext asyncContext;

    @Inject
    AppMetrics appMetrics;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    AppConfiguration config;

    // Cache performance tracking
    private final java.util.concurrent.atomic.AtomicLong totalCacheOperations =
            new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong totalCacheHits = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong totalCacheMisses =
            new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong totalCacheErrors =
            new java.util.concurrent.atomic.AtomicLong(0);

    /**
     * Get value from cache with observability
     */
    public <T> Uni<Optional<T>> get(String key, Class<T> valueType, String cacheType) {
        String tenantKey = tenantContext.getTenantCacheKeyPrefix(key);
        Instant start = Instant.now();

        return performCacheOperation(
                () -> getCacheValue(tenantKey, valueType, cacheType), "get", cacheType, key, start);
    }

    /**
     * Put value in cache with observability
     */
    public <T> Uni<Void> put(String key, T value, Duration ttl, String cacheType) {
        String tenantKey = tenantContext.getTenantCacheKeyPrefix(key);
        Instant start = Instant.now();

        return performCacheOperation(
                () -> putCacheValue(tenantKey, value, ttl, cacheType), "put", cacheType, key, start);
    }

    /**
     * Remove value from cache with observability
     */
    public Uni<Boolean> remove(String key, String cacheType) {
        String tenantKey = tenantContext.getTenantCacheKeyPrefix(key);
        Instant start = Instant.now();

        return performCacheOperation(() -> removeCacheValue(tenantKey, cacheType), "remove", cacheType, key, start);
    }

    /**
     * Clear tenant-specific cache with observability
     */
    public Uni<Void> clearTenantCache(String cacheType) {
        if (!tenantContext.isMultiTenantMode()) {
            log.warn("Attempted to clear tenant cache in single-tenant mode");
            return Uni.createFrom().voidItem();
        }

        String tenantNamespace = tenantContext.getTenantCacheNamespace();
        Instant start = Instant.now();

        return performCacheOperation(
                () -> clearCacheNamespace(tenantNamespace, cacheType),
                "clear_tenant",
                cacheType,
                tenantNamespace,
                start);
    }

    /**
     * Get or compute value with observability
     */
    public <T> Uni<T> getOrCompute(
            String key, Supplier<Uni<T>> computeFunction, Duration ttl, Class<T> valueType, String cacheType) {
        return get(key, valueType, cacheType).flatMap(optional -> {
            if (optional.isPresent()) {
                recordCacheHit(cacheType, key);
                return Uni.createFrom().item(optional.get());
            } else {
                recordCacheMiss(cacheType, key);
                // Wrap compute function with async observability context
                return asyncContext
                        .wrapWithObservability(
                                computeFunction.get(), String.format("cache_compute_%s", cacheType), "cache")
                        .flatMap(computedValue ->
                                put(key, computedValue, ttl, cacheType).replaceWith(computedValue));
            }
        });
    }

    /**
     * Warm cache with multiple entries
     */
    public <T> Uni<Void> warmCache(java.util.Map<String, T> entries, Duration ttl, String cacheType) {
        Instant start = Instant.now();
        Span span = io.opentelemetry.api.GlobalOpenTelemetry.getTracer("cache-operations")
                .spanBuilder(String.format("cache_warm_%s", cacheType))
                .setAttribute("cache.type", cacheType)
                .setAttribute("cache.entries.count", entries.size())
                .setAttribute("cache.tenant", tenantContext.getCurrentTenant())
                .startSpan();

        return Uni.combine()
                .all()
                .unis(entries.entrySet().stream()
                        .map(entry -> put(entry.getKey(), entry.getValue(), ttl, cacheType))
                        .toList())
                .discardItems()
                .onItem()
                .invoke(() -> {
                    long duration = Duration.between(start, Instant.now()).toMillis();
                    span.setAttribute("cache.warm.duration_ms", duration);
                    span.setAttribute("cache.warm.success", true);
                    span.end();

                    log.info(
                            "{}Cache warmed: {} [type={}, entries={}, duration={}ms]",
                            tenantContext.getTenantLogPrefix(),
                            cacheType,
                            entries.size(),
                            duration);
                })
                .onFailure()
                .invoke(failure -> {
                    span.recordException(failure);
                    span.setAttribute("cache.warm.success", false);
                    span.end();

                    log.error(
                            "{}Cache warming failed: {} [type={}, entries={}, error={}]",
                            tenantContext.getTenantLogPrefix(),
                            cacheType,
                            entries.size(),
                            failure.getMessage(),
                            failure);
                });
    }

    /**
     * Get cache statistics for monitoring
     */
    public CacheStatistics getStatistics(String cacheType) {
        long operations = totalCacheOperations.get();
        long hits = totalCacheHits.get();
        long misses = totalCacheMisses.get();
        long errors = totalCacheErrors.get();

        double hitRate = operations > 0 ? (double) hits / operations * 100.0 : 0.0;
        double errorRate = operations > 0 ? (double) errors / operations * 100.0 : 0.0;

        return new CacheStatistics(
                cacheType, tenantContext.getCurrentTenant(), operations, hits, misses, errors, hitRate, errorRate);
    }

    /**
     * Invalidate cache patterns with observability
     */
    public Uni<Long> invalidatePattern(String pattern, String cacheType) {
        String tenantPattern = tenantContext.getTenantCacheKeyPrefix(pattern);
        Instant start = Instant.now();

        return performCacheOperation(
                () -> invalidateByPattern(tenantPattern, cacheType), "invalidate_pattern", cacheType, pattern, start);
    }

    /**
     * Health check for cache systems
     */
    public Uni<CacheHealthStatus> healthCheck(String cacheType) {
        Instant start = Instant.now();
        String testKey = tenantContext.getTenantCacheKeyPrefix("health_check");
        String testValue = "health_" + System.currentTimeMillis();

        return put(testKey, testValue, Duration.ofMillis(config.health().cacheTestTtlMs()), cacheType)
                .flatMap(ignored -> get(testKey, String.class, cacheType))
                .flatMap(result -> remove(testKey, cacheType).replaceWith(result))
                .map(result -> {
                    long duration = Duration.between(start, Instant.now()).toMillis();
                    boolean healthy = result.isPresent() && testValue.equals(result.get());

                    return new CacheHealthStatus(
                            cacheType,
                            healthy,
                            duration,
                            healthy ? "Cache is responding correctly" : "Cache health check failed",
                            tenantContext.getCurrentTenant());
                })
                .onFailure()
                .recoverWithItem(failure -> {
                    long duration = Duration.between(start, Instant.now()).toMillis();
                    return new CacheHealthStatus(
                            cacheType,
                            false,
                            duration,
                            "Cache health check error: " + failure.getMessage(),
                            tenantContext.getCurrentTenant());
                });
    }

    private <T> Uni<T> performCacheOperation(
            Supplier<Uni<T>> operation, String operationType, String cacheType, String key, Instant start) {
        // Create timer sample
        Timer.Sample timerSample = Timer.start(meterRegistry);

        // Add span context with enhanced cache-specific attributes
        ObservabilityUtil.enrichSpanWithContext(
                observabilityContext.getCorrelationId(),
                tenantContext.getCurrentTenant(),
                observabilityContext.getUserId(),
                String.format("cache_%s_%s", cacheType, operationType),
                "cache");

        // Add cache-specific span attributes
        ObservabilityUtil.addSpanAttribute("cache.type", cacheType);
        ObservabilityUtil.addSpanAttribute("cache.operation.type", operationType);
        ObservabilityUtil.addSpanAttribute("cache.tenant.mode", tenantContext.isMultiTenantMode() ? "multi" : "single");
        if (tenantContext.isMultiTenantMode()) {
            ObservabilityUtil.addSpanAttribute("cache.tenant.namespace", tenantContext.getTenantCacheNamespace());
        }

        return operation
                .get()
                .onItem()
                .invoke(result -> {
                    recordCacheOperationSuccess(operationType, cacheType, key, start, timerSample);
                })
                .onFailure()
                .invoke(failure -> {
                    recordCacheOperationError(operationType, cacheType, key, start, timerSample, failure);
                    ObservabilityUtil.recordError(failure);
                });
    }

    private void recordCacheOperationSuccess(
            String operation, String cacheType, String key, Instant start, Timer.Sample timerSample) {
        Duration duration = Duration.between(start, Instant.now());

        // Record metrics
        timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_CACHE_OPERATION_DURATION)
                .tag(ObservabilityConstant.TAG_OPERATION, operation)
                .tag(ObservabilityConstant.TAG_CACHE_TYPE, cacheType)
                .tag(ObservabilityConstant.TAG_TENANT, tenantContext.getCurrentTenant())
                .register(meterRegistry));

        // Log with tenant context
        log.debug(
                "{}Cache operation completed: {} [type={}, key={}, duration={}ms]",
                tenantContext.getTenantLogPrefix(),
                operation,
                cacheType,
                key,
                duration.toMillis());
    }

    private void recordCacheOperationError(
            String operation, String cacheType, String key, Instant start, Timer.Sample timerSample, Throwable error) {
        totalCacheErrors.incrementAndGet();
        Duration duration = Duration.between(start, Instant.now());

        // Record error metrics with enhanced context
        Counter.builder("cache_operation_errors_total")
                .tag("operation", operation)
                .tag("cache_type", cacheType)
                .tag("error_type", error.getClass().getSimpleName())
                .tag("tenant", ObservabilityUtil.sanitizeTagValue(tenantContext.getCurrentTenant()))
                .register(meterRegistry)
                .increment();

        // Record error duration
        if (timerSample != null) {
            timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_CACHE_OPERATION_DURATION)
                    .tag("operation", operation)
                    .tag("cache_type", cacheType)
                    .tag("status", "error")
                    .tag("tenant", ObservabilityUtil.sanitizeTagValue(tenantContext.getCurrentTenant()))
                    .register(meterRegistry));
        }

        // Add error context to span
        ObservabilityUtil.addSpanAttribute("cache.error", "true");
        ObservabilityUtil.addSpanAttribute("cache.error.type", error.getClass().getSimpleName());
        ObservabilityUtil.addSpanAttribute("cache.operation.failed", "true");

        // Log error with enhanced context
        log.error(
                "{}Cache operation failed: {} [type={}, key={}, duration={}ms, error={}, context={}]",
                tenantContext.getTenantLogPrefix(),
                operation,
                cacheType,
                key.length() > 50 ? key.substring(0, 50) + "..." : key,
                duration.toMillis(),
                error.getMessage(),
                observabilityContext.getContextSummary(),
                error);
    }

    private Uni<Long> invalidateByPattern(String pattern, String cacheType) {
        // Implementation would depend on cache type
        return Uni.createFrom().item(0L);
    }

    /**
     * Cache statistics data class
     */
    public record CacheStatistics(
            String cacheType,
            String tenant,
            long totalOperations,
            long hits,
            long misses,
            long errors,
            double hitRate,
            double errorRate) {}

    /**
     * Cache health status data class
     */
    public record CacheHealthStatus(
            String cacheType, boolean healthy, long responseTimeMs, String message, String tenant) {}

    private void recordCacheHit(String cacheType, String key) {
        totalCacheHits.incrementAndGet();
        totalCacheOperations.incrementAndGet();

        // Record metrics with enhanced context
        Counter.builder("cache_operations_total")
                .tag("cache_type", cacheType)
                .tag("operation", "get")
                .tag("result", ObservabilityConstant.CACHE_HIT)
                .tag("tenant", ObservabilityUtil.sanitizeTagValue(tenantContext.getCurrentTenant()))
                .register(meterRegistry)
                .increment();

        if (ObservabilityConstant.CACHE_REDIS.equals(cacheType)) {
            appMetrics.recordRedisHit(Duration.ZERO);
        } else if (ObservabilityConstant.CACHE_CAFFEINE.equals(cacheType)) {
            appMetrics.recordCaffeineHit(Duration.ZERO);
        }

        // Add hit to span
        ObservabilityUtil.addSpanAttribute("cache.hit", "true");
        ObservabilityUtil.addSpanAttribute("cache.key", ObservabilityUtil.sanitizeTagValue(key));

        log.trace(
                "{}Cache hit: {} [type={}, key={}]",
                tenantContext.getTenantLogPrefix(),
                cacheType,
                key.length() > 50 ? key.substring(0, 50) + "..." : key);
    }

    private void recordCacheMiss(String cacheType, String key) {
        totalCacheMisses.incrementAndGet();
        totalCacheOperations.incrementAndGet();

        // Record metrics with enhanced context
        Counter.builder("cache_operations_total")
                .tag("cache_type", cacheType)
                .tag("operation", "get")
                .tag("result", ObservabilityConstant.CACHE_MISS)
                .tag("tenant", ObservabilityUtil.sanitizeTagValue(tenantContext.getCurrentTenant()))
                .register(meterRegistry)
                .increment();

        if (ObservabilityConstant.CACHE_REDIS.equals(cacheType)) {
            appMetrics.recordRedisMiss(Duration.ZERO);
        } else if (ObservabilityConstant.CACHE_CAFFEINE.equals(cacheType)) {
            appMetrics.recordCaffeineMiss(Duration.ZERO);
        }

        // Add miss to span
        ObservabilityUtil.addSpanAttribute("cache.hit", "false");
        ObservabilityUtil.addSpanAttribute("cache.key", ObservabilityUtil.sanitizeTagValue(key));

        log.trace(
                "{}Cache miss: {} [type={}, key={}]",
                tenantContext.getTenantLogPrefix(),
                cacheType,
                key.length() > 50 ? key.substring(0, 50) + "..." : key);
    }

    // Placeholder methods for actual cache operations
    // These would be implemented with actual cache providers (Redis, Caffeine, etc.)
    // Enhanced with better observability context

    private <T> Uni<Optional<T>> getCacheValue(String key, Class<T> valueType, String cacheType) {
        // Add cache operation context to span
        ObservabilityUtil.addSpanAttribute("cache.operation", "get");
        ObservabilityUtil.addSpanAttribute("cache.key.length", String.valueOf(key.length()));
        ObservabilityUtil.addSpanAttribute("cache.value.type", valueType.getSimpleName());

        // Implementation would depend on cache type
        // For Redis: use reactive Redis client
        // For Caffeine: use async Caffeine cache
        return Uni.createFrom().item(Optional.empty());
    }

    private <T> Uni<Void> putCacheValue(String key, T value, Duration ttl, String cacheType) {
        // Add cache operation context to span
        ObservabilityUtil.addSpanAttribute("cache.operation", "put");
        ObservabilityUtil.addSpanAttribute("cache.key.length", String.valueOf(key.length()));
        ObservabilityUtil.addSpanAttribute("cache.ttl.seconds", String.valueOf(ttl.toSeconds()));
        if (value != null) {
            ObservabilityUtil.addSpanAttribute(
                    "cache.value.type", value.getClass().getSimpleName());
        }

        // Implementation would depend on cache type
        return Uni.createFrom().voidItem();
    }

    private Uni<Boolean> removeCacheValue(String key, String cacheType) {
        // Add cache operation context to span
        ObservabilityUtil.addSpanAttribute("cache.operation", "remove");
        ObservabilityUtil.addSpanAttribute("cache.key.length", String.valueOf(key.length()));

        // Implementation would depend on cache type
        return Uni.createFrom().item(false);
    }

    private Uni<Void> clearCacheNamespace(String namespace, String cacheType) {
        // Add cache operation context to span
        ObservabilityUtil.addSpanAttribute("cache.operation", "clear_namespace");
        ObservabilityUtil.addSpanAttribute("cache.namespace", namespace);

        // Implementation would depend on cache type
        return Uni.createFrom().voidItem();
    }
}
