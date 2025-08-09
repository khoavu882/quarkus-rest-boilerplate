package com.github.kaivu.adapter.in.rest;

import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.context.TenantObservabilityContext;
import com.github.kaivu.common.service.ObservableCacheService;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.annotations.Observability;
import com.github.kaivu.config.metrics.AppMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST resource providing observability metrics and insights for monitoring
 * and troubleshooting in production environments.
 *
 * Features:
 * - Application performance metrics
 * - Tenant-specific metrics and isolation status
 * - Cache performance analytics
 * - Database connection health
 * - Error correlation and analysis
 * - Real-time observability context information
 */
@Slf4j
@RequestScoped
@Path("/api/observability")
@Tag(name = "Observability", description = "Observability and monitoring endpoints")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class ObservabilityResource {

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    TenantObservabilityContext tenantContext;

    @Inject
    AppMetrics appMetrics;

    @Inject
    ObservableCacheService cacheService;

    @Inject
    MeterRegistry meterRegistry;

    @GET
    @Path("/context")
    @Operation(
            summary = "Get current observability context",
            description =
                    "Returns the current request's observability context including correlation ID, tenant info, and tracing details")
    @APIResponse(
            responseCode = "200",
            description = "Current observability context",
            content = @Content(schema = @Schema(implementation = ObservabilityContextVM.class)))
    @Observability("get_observability_context")
    public ObservabilityContextVM getContext() {
        return new ObservabilityContextVM(
                observabilityContext.getCorrelationId(),
                observabilityContext.getTraceId(),
                observabilityContext.getSpanId(),
                observabilityContext.getTenantId(),
                observabilityContext.getUserId(),
                observabilityContext.getSessionId(),
                observabilityContext.getRequestPath(),
                observabilityContext.getHttpMethod(),
                observabilityContext.getUserAgent(),
                observabilityContext.getRemoteIp(),
                observabilityContext.getRequestStartTime(),
                tenantContext.getCurrentTenant(),
                tenantContext.isMultiTenantMode(),
                tenantContext.getTenantIsolationSummary(),
                observabilityContext.getCustomAttributes());
    }

    @GET
    @Path("/metrics")
    @Operation(
            summary = "Get application metrics summary",
            description = "Returns comprehensive application performance metrics and statistics")
    @APIResponse(
            responseCode = "200",
            description = "Application metrics",
            content = @Content(schema = @Schema(implementation = ApplicationMetricsVM.class)))
    @Observability("get_application_metrics")
    public ApplicationMetricsVM getMetrics() {
        AppMetrics.AppStats appStats = appMetrics.getAppStats();
        AppMetrics.HealthSummary healthSummary = appMetrics.getHealthSummary();

        return new ApplicationMetricsVM(
                appStats.totalRequests(),
                appStats.totalErrors(),
                appStats.activeTenants(),
                appStats.uptimeSeconds(),
                healthSummary.healthy(),
                healthSummary.errorRate(),
                healthSummary.activeConnections(),
                healthSummary.cacheHitRate(),
                Instant.now().toString(),
                tenantContext.getCurrentTenant(),
                appStats.cacheStats(),
                appStats.databaseStats());
    }

    @GET
    @Path("/metrics/tenant/{tenantId}")
    @Operation(
            summary = "Get tenant-specific metrics",
            description = "Returns metrics and statistics for a specific tenant")
    @APIResponse(
            responseCode = "200",
            description = "Tenant metrics",
            content = @Content(schema = @Schema(implementation = TenantMetricsVM.class)))
    @Observability("get_tenant_metrics")
    public Uni<Response> getTenantMetrics(@PathParam("tenantId") String tenantId) {
        // Validate tenant access
        if (tenantContext.isMultiTenantMode() && !tenantContext.canAccessTenantData(tenantId)) {
            return Uni.createFrom()
                    .item(Response.status(Response.Status.FORBIDDEN)
                            .entity(Map.of("error", "Access denied to tenant data"))
                            .build());
        }

        AppMetrics.TenantStats tenantStats = appMetrics.getTenantStats(tenantId);

        TenantMetricsVM metrics = new TenantMetricsVM(
                tenantStats.tenantId(),
                tenantStats.requests(),
                tenantStats.errors(),
                tenantStats.errorRate(),
                tenantContext.getTenantDatabaseSchema(),
                tenantContext.getTenantCacheNamespace(),
                Instant.now().toString());

        return Uni.createFrom().item(Response.ok(metrics).build());
    }

    @GET
    @Path("/cache")
    @Operation(
            summary = "Get cache performance statistics",
            description = "Returns comprehensive cache performance metrics for all cache types")
    @APIResponse(
            responseCode = "200",
            description = "Cache statistics",
            content = @Content(schema = @Schema(implementation = CacheMetricsVM.class)))
    @Observability("get_cache_metrics")
    public CacheMetricsVM getCacheMetrics() {
        AppMetrics.CacheStats cacheStats = appMetrics.getCacheStats();

        return new CacheMetricsVM(
                cacheStats.redisHits(),
                cacheStats.redisMisses(),
                cacheStats.redisErrors(),
                cacheStats.redisHitRate(),
                cacheStats.caffeineHits(),
                cacheStats.caffeineMisses(),
                cacheStats.caffeineSize(),
                cacheStats.caffeineHitRate(),
                cacheStats.overallHitRate(),
                tenantContext.getCurrentTenant(),
                Instant.now().toString());
    }

    @GET
    @Path("/cache/health")
    @Operation(
            summary = "Get cache system health status",
            description = "Performs health checks on all cache systems and returns status")
    @APIResponse(responseCode = "200", description = "Cache health status")
    @Observability("get_cache_health")
    public Uni<Map<String, Object>> getCacheHealth() {
        return Uni.combine()
                .all()
                .unis(cacheService.healthCheck("redis"), cacheService.healthCheck("caffeine"))
                .with((redisHealth, caffeineHealth) -> {
                    Map<String, Object> health = new HashMap<>();
                    health.put(
                            "redis",
                            Map.of(
                                    "healthy", redisHealth.healthy(),
                                    "responseTime", redisHealth.responseTimeMs(),
                                    "message", redisHealth.message()));
                    health.put(
                            "caffeine",
                            Map.of(
                                    "healthy", caffeineHealth.healthy(),
                                    "responseTime", caffeineHealth.responseTimeMs(),
                                    "message", caffeineHealth.message()));
                    health.put("overall", redisHealth.healthy() && caffeineHealth.healthy());
                    health.put("tenant", tenantContext.getCurrentTenant());
                    health.put("timestamp", Instant.now().toString());
                    return health;
                });
    }

    @GET
    @Path("/traces")
    @Operation(
            summary = "Get current tracing information",
            description = "Returns active tracing context and span information")
    @APIResponse(responseCode = "200", description = "Tracing information")
    @Observability("get_tracing_info")
    public Map<String, Object> getTracingInfo() {
        Map<String, Object> tracing = new HashMap<>();

        ObservabilityUtil.getCurrentTraceInfo()
                .ifPresentOrElse(
                        traceInfo -> {
                            tracing.put("active", true);
                            tracing.put("traceId", traceInfo.traceId());
                            tracing.put("spanId", traceInfo.spanId());
                            tracing.put("sampled", traceInfo.sampled());
                        },
                        () -> tracing.put("active", false));

        tracing.put("correlationId", observabilityContext.getCorrelationId());
        tracing.put("contextSummary", observabilityContext.getContextSummary());
        tracing.put("tenantContext", tenantContext.getTenantIsolationSummary());
        tracing.put("timestamp", Instant.now().toString());

        return tracing;
    }

    @GET
    @Path("/metrics/custom")
    @Operation(
            summary = "Get custom application metrics",
            description = "Returns all registered custom metrics from the meter registry")
    @APIResponse(responseCode = "200", description = "Custom metrics")
    @Observability("get_custom_metrics")
    public Map<String, Object> getCustomMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Get all registered meters
            List<String> meterNames = meterRegistry.getMeters().stream()
                    .map(meter -> meter.getId().getName())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            metrics.put("registeredMeters", meterNames);
            metrics.put("totalMeterCount", meterRegistry.getMeters().size());

            // Get specific application metrics
            Search search = meterRegistry.find("http_requests_total");
            if (search.counter() != null) {
                metrics.put("httpRequestsTotal", search.counter().count());
            }

            search = meterRegistry.find("database_operation_total");
            if (search.counter() != null) {
                metrics.put("databaseOperationsTotal", search.counter().count());
            }

            search = meterRegistry.find("cache_operations_total");
            if (search.counter() != null) {
                metrics.put("cacheOperationsTotal", search.counter().count());
            }

        } catch (Exception e) {
            metrics.put("error", "Failed to retrieve custom metrics: " + e.getMessage());
            log.warn("Error retrieving custom metrics: {}", e.getMessage());
        }

        metrics.put("tenant", tenantContext.getCurrentTenant());
        metrics.put("timestamp", Instant.now().toString());

        return metrics;
    }

    @GET
    @Path("/health/summary")
    @Operation(
            summary = "Get comprehensive health summary",
            description = "Returns overall system health with detailed component status")
    @APIResponse(responseCode = "200", description = "Health summary")
    @Observability("get_health_summary")
    public Map<String, Object> getHealthSummary() {
        Map<String, Object> health = new HashMap<>();

        AppMetrics.HealthSummary healthSummary = appMetrics.getHealthSummary();
        AppMetrics.AppStats appStats = appMetrics.getAppStats();

        health.put("overall", healthSummary.healthy());
        health.put("uptime", healthSummary.uptimeSeconds());
        health.put("errorRate", healthSummary.errorRate());
        health.put("activeConnections", healthSummary.activeConnections());
        health.put("cacheHitRate", healthSummary.cacheHitRate());

        // Component health
        health.put(
                "components",
                Map.of(
                        "database",
                                Map.of(
                                        "healthy", appStats.databaseStats().healthy(),
                                        "activeConnections",
                                                appStats.databaseStats().activeConnections(),
                                        "waitingConnections",
                                                appStats.databaseStats().waitingConnections()),
                        "cache",
                                Map.of(
                                        "healthy",
                                        appStats.cacheStats().redisErrors() < 10,
                                        "hitRate",
                                        appStats.cacheStats().overallHitRate(),
                                        "totalOperations",
                                        appStats.cacheStats().redisHits()
                                                + appStats.cacheStats().redisMisses()),
                        "tenant",
                                Map.of(
                                        "mode", tenantContext.isMultiTenantMode() ? "multi" : "single",
                                        "currentTenant", tenantContext.getCurrentTenant(),
                                        "activeTenants", appStats.activeTenants())));

        health.put(
                "metrics",
                Map.of(
                        "totalRequests", appStats.totalRequests(),
                        "totalErrors", appStats.totalErrors(),
                        "requestsPerSecond", appStats.totalRequests() / Math.max(1, appStats.uptimeSeconds())));

        health.put("timestamp", Instant.now().toString());
        health.put("correlationId", observabilityContext.getCorrelationId());

        return health;
    }

    // View Models for API responses

    public record ObservabilityContextVM(
            String correlationId,
            String traceId,
            String spanId,
            String tenantId,
            String userId,
            String sessionId,
            String requestPath,
            String httpMethod,
            String userAgent,
            String remoteIp,
            Instant requestStartTime,
            String currentTenant,
            boolean multiTenantMode,
            String tenantIsolationSummary,
            Map<String, String> customAttributes) {}

    public record ApplicationMetricsVM(
            long totalRequests,
            long totalErrors,
            int activeTenants,
            double uptimeSeconds,
            boolean healthy,
            double errorRate,
            long activeConnections,
            double cacheHitRate,
            String timestamp,
            String tenant,
            AppMetrics.CacheStats cacheStats,
            AppMetrics.DatabaseStats databaseStats) {}

    public record TenantMetricsVM(
            String tenantId,
            long requests,
            long errors,
            double errorRate,
            String databaseSchema,
            String cacheNamespace,
            String timestamp) {}

    public record CacheMetricsVM(
            long redisHits,
            long redisMisses,
            long redisErrors,
            double redisHitRate,
            long caffeineHits,
            long caffeineMisses,
            long caffeineSize,
            double caffeineHitRate,
            double overallHitRate,
            String tenant,
            String timestamp) {}
}
