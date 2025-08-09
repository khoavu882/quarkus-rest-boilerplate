package com.github.kaivu.config.health;

import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.context.TenantObservabilityContext;
import com.github.kaivu.common.service.ObservableCacheService;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.metrics.AppMetrics;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive health check that verifies observability infrastructure
 * including metrics collection, tracing, caching, and tenant isolation.
 *
 * Features:
 * - Observability system health verification
 * - Tenant isolation system checks
 * - Cache system health monitoring
 * - Metrics collection system verification
 * - Performance threshold monitoring
 * - Integration with monitoring systems
 */
@Slf4j
@ApplicationScoped
@Readiness
public class ObservabilityHealthCheck implements HealthCheck {

    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(10);
    private static final double MAX_ERROR_RATE_THRESHOLD = 5.0; // 5% error rate threshold
    private static final long MAX_RESPONSE_TIME_THRESHOLD = 1000; // 1 second threshold

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    TenantObservabilityContext tenantContext;

    @Inject
    AppMetrics appMetrics;

    @Inject
    ObservableCacheService cacheService;

    @Override
    public HealthCheckResponse call() {
        Instant start = Instant.now();
        String healthCheckId = generateHealthCheckId();

        // Set up observability context for health check
        initializeHealthCheckContext(healthCheckId);

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("observability-infrastructure");

        try {
            // Perform comprehensive health checks
            CompletableFuture<HealthResult> metricsHealth = checkMetricsSystemHealth();
            CompletableFuture<HealthResult> cacheHealth = checkCacheSystemHealth();
            CompletableFuture<HealthResult> tracingHealth = checkTracingSystemHealth();
            CompletableFuture<HealthResult> tenantHealth = checkTenantIsolationHealth();

            // Wait for all health checks with timeout
            CompletableFuture<Void> allChecks =
                    CompletableFuture.allOf(metricsHealth, cacheHealth, tracingHealth, tenantHealth);

            allChecks.get(HEALTH_CHECK_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            // Collect results
            HealthResult metrics = metricsHealth.get();
            HealthResult cache = cacheHealth.get();
            HealthResult tracing = tracingHealth.get();
            HealthResult tenant = tenantHealth.get();

            // Determine overall health status
            boolean overallHealthy = metrics.healthy() && cache.healthy() && tracing.healthy() && tenant.healthy();

            long totalDuration = Duration.between(start, Instant.now()).toMillis();

            // Build response with detailed information
            responseBuilder
                    .status(overallHealthy)
                    .withData("healthCheckId", healthCheckId)
                    .withData("totalDurationMs", totalDuration)
                    .withData("timestamp", Instant.now().toString())
                    .withData("metrics.status", metrics.healthy() ? "UP" : "DOWN")
                    .withData("metrics.details", metrics.details())
                    .withData("cache.status", cache.healthy() ? "UP" : "DOWN")
                    .withData("cache.details", cache.details())
                    .withData("tracing.status", tracing.healthy() ? "UP" : "DOWN")
                    .withData("tracing.details", tracing.details())
                    .withData("tenant.status", tenant.healthy() ? "UP" : "DOWN")
                    .withData("tenant.details", tenant.details());

            // Add performance metrics
            addPerformanceMetrics(responseBuilder);

            // Add tenant context if available
            addTenantContextInfo(responseBuilder);

            // Log health check results
            logHealthCheckResults(healthCheckId, overallHealthy, totalDuration, metrics, cache, tracing, tenant);

        } catch (Exception e) {
            long totalDuration = Duration.between(start, Instant.now()).toMillis();

            responseBuilder
                    .down()
                    .withData("healthCheckId", healthCheckId)
                    .withData("error", e.getMessage())
                    .withData("errorType", e.getClass().getSimpleName())
                    .withData("totalDurationMs", totalDuration);

            log.error(
                    "Health check failed [id={}, duration={}ms]: {}", healthCheckId, totalDuration, e.getMessage(), e);

            // Record error in observability systems
            ObservabilityUtil.recordError(e);
        } finally {
            cleanupHealthCheckContext();
        }

        return responseBuilder.build();
    }

    private void initializeHealthCheckContext(String healthCheckId) {
        try {
            // Initialize minimal observability context for health check
            observabilityContext.setCorrelationId(healthCheckId);
            observabilityContext.setRequestPath("/q/health/ready");
            observabilityContext.setHttpMethod("GET");
            observabilityContext.setRequestStartTime(Instant.now());

            // Set up MDC for structured logging
            ObservabilityUtil.setMDCContext(healthCheckId, null, null, null, null, "/q/health/ready", "GET");

        } catch (Exception e) {
            log.debug("Error initializing health check context: {}", e.getMessage());
        }
    }

    private CompletableFuture<HealthResult> checkMetricsSystemHealth() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if metrics collection is working
                AppMetrics.AppStats stats = appMetrics.getAppStats();
                AppMetrics.HealthSummary health = appMetrics.getHealthSummary();

                boolean healthy = health.healthy() && health.errorRate() < MAX_ERROR_RATE_THRESHOLD;

                String details = String.format(
                        "requests=%d, errors=%d, errorRate=%.2f%%, uptime=%.0fs",
                        stats.totalRequests(), stats.totalErrors(), health.errorRate(), health.uptimeSeconds());

                return new HealthResult(healthy, details);

            } catch (Exception e) {
                return new HealthResult(false, "Metrics system error: " + e.getMessage());
            }
        });
    }

    private CompletableFuture<HealthResult> checkCacheSystemHealth() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check Redis cache health
                return cacheService
                        .healthCheck("redis")
                        .map(status -> new HealthResult(
                                status.healthy() && status.responseTimeMs() < MAX_RESPONSE_TIME_THRESHOLD,
                                String.format(
                                        "responseTime=%dms, message=%s", status.responseTimeMs(), status.message())))
                        .await()
                        .atMost(Duration.ofSeconds(5));

            } catch (Exception e) {
                return new HealthResult(false, "Cache system error: " + e.getMessage());
            }
        });
    }

    private CompletableFuture<HealthResult> checkTracingSystemHealth() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if tracing context is available and working
                var traceInfo = ObservabilityUtil.getCurrentTraceInfo();
                boolean hasTracing = traceInfo.isPresent();

                // Test span creation and attribute setting
                ObservabilityUtil.addSpanAttribute("health.check", "tracing");

                String details = hasTracing
                        ? String.format(
                                "traceId=%s, spanId=%s",
                                traceInfo.get().traceId(), traceInfo.get().spanId())
                        : "No active tracing context";

                return new HealthResult(true, details); // Tracing is optional, so always healthy

            } catch (Exception e) {
                return new HealthResult(false, "Tracing system error: " + e.getMessage());
            }
        });
    }

    private CompletableFuture<HealthResult> checkTenantIsolationHealth() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check tenant context functionality
                boolean multiTenantMode = tenantContext.isMultiTenantMode();
                String currentTenant = tenantContext.getCurrentTenant();
                String isolation = tenantContext.getTenantIsolationSummary();

                // Test tenant-specific operations
                String testKey = "health_check_key";
                String tenantKey = tenantContext.getTenantCacheKeyPrefix(testKey);
                String dbSchema = tenantContext.getTenantDatabaseSchema();

                boolean healthy = currentTenant != null && !currentTenant.isEmpty();

                String details = String.format(
                        "mode=%s, tenant=%s, isolation=%s, dbSchema=%s",
                        multiTenantMode ? "multi" : "single", currentTenant, isolation, dbSchema);

                return new HealthResult(healthy, details);

            } catch (Exception e) {
                return new HealthResult(false, "Tenant isolation error: " + e.getMessage());
            }
        });
    }

    private void addPerformanceMetrics(HealthCheckResponseBuilder responseBuilder) {
        try {
            AppMetrics.CacheStats cacheStats = appMetrics.getCacheStats();
            AppMetrics.DatabaseStats dbStats = appMetrics.getDatabaseStats();

            responseBuilder
                    .withData("performance.cache.hitRate", (long) cacheStats.overallHitRate())
                    .withData("performance.cache.redisHits", cacheStats.redisHits())
                    .withData("performance.cache.redisMisses", cacheStats.redisMisses())
                    .withData("performance.cache.redisErrors", cacheStats.redisErrors())
                    .withData("performance.db.activeConnections", dbStats.activeConnections())
                    .withData("performance.db.waitingConnections", dbStats.waitingConnections());

        } catch (Exception e) {
            responseBuilder.withData("performance.error", e.getMessage());
        }
    }

    private void addTenantContextInfo(HealthCheckResponseBuilder responseBuilder) {
        try {
            if (tenantContext.isMultiTenantMode()) {
                responseBuilder
                        .withData("tenant.id", tenantContext.getCurrentTenant())
                        .withData("tenant.mode", "multi")
                        .withData("tenant.schema", tenantContext.getTenantDatabaseSchema())
                        .withData("tenant.cacheNamespace", tenantContext.getTenantCacheNamespace());
            } else {
                responseBuilder.withData("tenant.mode", "single");
            }

        } catch (Exception e) {
            responseBuilder.withData("tenant.error", e.getMessage());
        }
    }

    private void logHealthCheckResults(
            String healthCheckId,
            boolean overallHealthy,
            long duration,
            HealthResult metrics,
            HealthResult cache,
            HealthResult tracing,
            HealthResult tenant) {
        if (overallHealthy) {
            log.info(
                    "Health check passed [id={}, duration={}ms] - metrics: {}, cache: {}, tracing: {}, tenant: {}",
                    healthCheckId,
                    duration,
                    metrics.healthy() ? "OK" : "FAIL",
                    cache.healthy() ? "OK" : "FAIL",
                    tracing.healthy() ? "OK" : "FAIL",
                    tenant.healthy() ? "OK" : "FAIL");
        } else {
            log.warn(
                    "Health check failed [id={}, duration={}ms] - metrics: [{}], cache: [{}], tracing: [{}], tenant: [{}]",
                    healthCheckId,
                    duration,
                    metrics.healthy() ? "OK" : "FAIL: " + metrics.details(),
                    cache.healthy() ? "OK" : "FAIL: " + cache.details(),
                    tracing.healthy() ? "OK" : "FAIL: " + tracing.details(),
                    tenant.healthy() ? "OK" : "FAIL: " + tenant.details());
        }
    }

    private void cleanupHealthCheckContext() {
        try {
            ObservabilityUtil.clearMDCContext();
        } catch (Exception e) {
            log.debug("Error cleaning up health check context: {}", e.getMessage());
        }
    }

    private String generateHealthCheckId() {
        return String.format(
                "hc-%d-%s",
                System.currentTimeMillis() % 1000000,
                java.util.UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Health result record for individual system checks
     */
    private record HealthResult(boolean healthy, String details) {}
}
