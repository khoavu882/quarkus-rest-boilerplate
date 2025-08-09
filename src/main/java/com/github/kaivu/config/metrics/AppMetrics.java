package com.github.kaivu.config.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Enhanced performance monitoring with observability context support.
 * Tracks cache performance, database connection health, and application metrics
 * with tenant isolation and comprehensive observability features.
 */
@Slf4j
@ApplicationScoped
public class AppMetrics {

    @Inject
    MeterRegistry meterRegistry;

    // Legacy counters (maintained for backward compatibility)
    private final LongAdder redisHits = new LongAdder();
    private final LongAdder redisMisses = new LongAdder();
    private final LongAdder redisErrors = new LongAdder();
    private final LongAdder caffeineHits = new LongAdder();
    private final LongAdder caffeineMisses = new LongAdder();
    private final AtomicLong caffeineSize = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong waitingConnections = new AtomicLong(0);
    private final AtomicLong connectionEvents = new AtomicLong(0);

    // Enhanced metrics with observability context
    private final Map<String, AtomicLong> tenantRequestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> tenantErrorCounts = new ConcurrentHashMap<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final Instant startTime = Instant.now();

    void onStart(@Observes StartupEvent ev) {
        log.info("Enhanced app metrics system initialized successfully with observability context support");

        // Register custom gauges for application metrics
        registerApplicationGauges();
    }

    private void registerApplicationGauges() {
        // Application uptime gauge
        meterRegistry.gauge("application_uptime_seconds", this, metrics -> Duration.between(startTime, Instant.now())
                .toSeconds());

        // Total requests gauge
        meterRegistry.gauge("application_total_requests", totalRequests);

        // Total errors gauge
        meterRegistry.gauge("application_total_errors", totalErrors);

        // Active connections gauge
        meterRegistry.gauge("database_active_connections", activeConnections);

        // Waiting connections gauge
        meterRegistry.gauge("database_waiting_connections", waitingConnections);
    }

    // Redis metrics recording methods
    public void recordRedisHit(Duration duration) {
        redisHits.increment();
        log.debug("Redis cache hit recorded in {}ms", duration.toMillis());
    }

    public void recordRedisMiss(Duration duration) {
        redisMisses.increment();
        log.debug("Redis cache miss recorded in {}ms", duration.toMillis());
    }

    public void recordRedisError() {
        redisErrors.increment();
        log.debug("Redis cache error recorded");
    }

    // Caffeine metrics recording methods
    public void recordCaffeineHit(Duration duration) {
        caffeineHits.increment();
        log.debug("Caffeine cache hit recorded in {}ms", duration.toMillis());
    }

    public void recordCaffeineMiss(Duration duration) {
        caffeineMisses.increment();
        log.debug("Caffeine cache miss recorded in {}ms", duration.toMillis());
    }

    public void updateCaffeineSize(long size) {
        caffeineSize.set(size);
    }

    // Database metrics recording methods
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
        connectionEvents.incrementAndGet();
    }

    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
        connectionEvents.incrementAndGet();
    }

    public void incrementWaitingConnections() {
        waitingConnections.incrementAndGet();
    }

    public void decrementWaitingConnections() {
        waitingConnections.decrementAndGet();
    }

    // Hit rate calculation methods
    public double getRedisHitRate() {
        long hits = redisHits.sum();
        long misses = redisMisses.sum();
        long total = hits + misses;

        if (total == 0) return 0.0;
        return ((double) hits / total) * 100.0;
    }

    public double getCaffeineHitRate() {
        long hits = caffeineHits.sum();
        long misses = caffeineMisses.sum();
        long total = hits + misses;

        if (total == 0) return 0.0;
        return ((double) hits / total) * 100.0;
    }

    public double getOverallHitRate() {
        long totalHits = redisHits.sum() + caffeineHits.sum();
        long totalMisses = redisMisses.sum() + caffeineMisses.sum();
        long total = totalHits + totalMisses;

        if (total == 0) return 0.0;
        return ((double) totalHits / total) * 100.0;
    }

    // Utility method to get cache statistics snapshot
    public CacheStats getCacheStats() {
        return new CacheStats(
                redisHits.sum(),
                redisMisses.sum(),
                redisErrors.sum(),
                caffeineHits.sum(),
                caffeineMisses.sum(),
                caffeineSize.get(),
                getRedisHitRate(),
                getCaffeineHitRate(),
                getOverallHitRate());
    }

    // Utility method to get database statistics snapshot
    public DatabaseStats getDatabaseStats() {
        return new DatabaseStats(
                activeConnections.get(),
                waitingConnections.get(),
                connectionEvents.get(),
                true // Simplified health check
                );
    }

    /**
     * Data class for cache statistics
     */
    public record CacheStats(
            long redisHits,
            long redisMisses,
            long redisErrors,
            long caffeineHits,
            long caffeineMisses,
            long caffeineSize,
            double redisHitRate,
            double caffeineHitRate,
            double overallHitRate) {}

    /**
     * Data class for database statistics
     */
    public record DatabaseStats(
            long activeConnections, long waitingConnections, long connectionEvents, boolean healthy) {}

    // Enhanced observability methods

    /**
     * Record a request with tenant context
     */
    public void recordRequest(String tenantId) {
        totalRequests.incrementAndGet();
        if (tenantId != null && !tenantId.isEmpty()) {
            tenantRequestCounts
                    .computeIfAbsent(tenantId, k -> new AtomicLong(0))
                    .incrementAndGet();
        }
    }

    /**
     * Record an error with tenant context
     */
    public void recordError(String tenantId, String errorType) {
        totalErrors.incrementAndGet();
        if (tenantId != null && !tenantId.isEmpty()) {
            tenantErrorCounts.computeIfAbsent(tenantId, k -> new AtomicLong(0)).incrementAndGet();
        }

        // Record error type metric
        meterRegistry
                .counter(
                        "application_errors_total",
                        "tenant",
                        tenantId != null ? tenantId : "default",
                        "error_type",
                        errorType)
                .increment();
    }

    /**
     * Get comprehensive application statistics
     */
    public AppStats getAppStats() {
        return new AppStats(
                totalRequests.get(),
                totalErrors.get(),
                tenantRequestCounts.size(),
                Duration.between(startTime, Instant.now()).toSeconds(),
                getCacheStats(),
                getDatabaseStats());
    }

    /**
     * Get tenant-specific statistics
     */
    public TenantStats getTenantStats(String tenantId) {
        long requests =
                tenantRequestCounts.getOrDefault(tenantId, new AtomicLong(0)).get();
        long errors =
                tenantErrorCounts.getOrDefault(tenantId, new AtomicLong(0)).get();
        double errorRate = requests > 0 ? ((double) errors / requests) * 100.0 : 0.0;

        return new TenantStats(tenantId, requests, errors, errorRate);
    }

    /**
     * Get all tenant statistics
     */
    public Map<String, TenantStats> getAllTenantStats() {
        Map<String, TenantStats> stats = new ConcurrentHashMap<>();
        tenantRequestCounts.keySet().forEach(tenantId -> stats.put(tenantId, getTenantStats(tenantId)));
        return stats;
    }

    /**
     * Reset tenant-specific metrics (useful for testing)
     */
    public void resetTenantMetrics(String tenantId) {
        tenantRequestCounts.remove(tenantId);
        tenantErrorCounts.remove(tenantId);
    }

    /**
     * Get system health summary
     */
    public HealthSummary getHealthSummary() {
        DatabaseStats dbStats = getDatabaseStats();
        CacheStats cacheStats = getCacheStats();

        boolean healthy = dbStats.healthy() && cacheStats.redisErrors() < 10; // Simple health check
        double errorRate = totalRequests.get() > 0 ? ((double) totalErrors.get() / totalRequests.get()) * 100.0 : 0.0;

        return new HealthSummary(
                healthy,
                errorRate,
                Duration.between(startTime, Instant.now()).toSeconds(),
                dbStats.activeConnections(),
                cacheStats.overallHitRate());
    }

    /**
     * Comprehensive application statistics
     */
    public record AppStats(
            long totalRequests,
            long totalErrors,
            int activeTenants,
            double uptimeSeconds,
            CacheStats cacheStats,
            DatabaseStats databaseStats) {}

    /**
     * Tenant-specific statistics
     */
    public record TenantStats(String tenantId, long requests, long errors, double errorRate) {}

    /**
     * System health summary
     */
    public record HealthSummary(
            boolean healthy, double errorRate, double uptimeSeconds, long activeConnections, double cacheHitRate) {}
}
