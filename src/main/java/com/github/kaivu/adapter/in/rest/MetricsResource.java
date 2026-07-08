package com.github.kaivu.adapter.in.rest;

import com.github.kaivu.config.metrics.AppMetrics;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

/**
 * REST endpoint for accessing performance metrics and monitoring data
 * Provides insights into cache performance and database connection health
 */
@Slf4j
@Path("/api/metrics")
@Tag(name = "System Metrics", description = "Performance and monitoring metrics")
public class MetricsResource {

    private final AppMetrics simpleMetrics;

    @Inject
    public MetricsResource(AppMetrics simpleMetrics) {
        this.simpleMetrics = simpleMetrics;
    }

    @GET
    @Path("/cache")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getCacheMetrics", summary = "Get cache performance metrics")
    @APIResponse(responseCode = "200", description = "Cache metrics retrieved successfully")
    public Uni<AppMetrics.CacheStats> getCacheMetrics() {
        return Uni.createFrom().item(() -> {
            log.debug("Retrieving cache performance metrics");
            return simpleMetrics.getCacheStats();
        });
    }

    @GET
    @Path("/database")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getDatabaseMetrics", summary = "Get database connection pool metrics")
    @APIResponse(responseCode = "200", description = "Database metrics retrieved successfully")
    public Uni<AppMetrics.DatabaseStats> getDatabaseMetrics() {
        return Uni.createFrom().item(() -> {
            log.debug("Retrieving database connection metrics");
            return simpleMetrics.getDatabaseStats();
        });
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getSystemHealth", summary = "Get overall system health metrics")
    @APIResponse(responseCode = "200", description = "System health metrics retrieved successfully")
    public Uni<SystemHealthMetrics> getSystemHealth() {
        return Uni.createFrom().item(() -> {
            log.debug("Retrieving system health metrics");

            var cacheStats = simpleMetrics.getCacheStats();
            var dbStats = simpleMetrics.getDatabaseStats();

            // Calculate overall system health score
            double healthScore = calculateHealthScore(cacheStats, dbStats);

            return new SystemHealthMetrics(
                    cacheStats, dbStats, healthScore, determineHealthStatus(healthScore), System.currentTimeMillis());
        });
    }

    @GET
    @Path("/performance")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getPerformanceMetrics", summary = "Get detailed performance metrics")
    @APIResponse(responseCode = "200", description = "Performance metrics retrieved successfully")
    public Uni<Map<String, Object>> getPerformanceMetrics() {
        return Uni.createFrom().item(() -> {
            log.debug("Retrieving detailed performance metrics");

            var cacheStats = simpleMetrics.getCacheStats();
            var dbStats = simpleMetrics.getDatabaseStats();

            return Map.of(
                    "cache",
                            Map.of(
                                    "redis",
                                            Map.of(
                                                    "hitRate", cacheStats.redisHitRate(),
                                                    "hits", cacheStats.redisHits(),
                                                    "misses", cacheStats.redisMisses(),
                                                    "errors", cacheStats.redisErrors()),
                                    "caffeine",
                                            Map.of(
                                                    "hitRate", cacheStats.caffeineHitRate(),
                                                    "hits", cacheStats.caffeineHits(),
                                                    "misses", cacheStats.caffeineMisses(),
                                                    "size", cacheStats.caffeineSize()),
                                    "overall", Map.of("hitRate", cacheStats.overallHitRate())),
                    "database",
                            Map.of(
                                    "connections",
                                            Map.of(
                                                    "active", dbStats.activeConnections(),
                                                    "waiting", dbStats.waitingConnections(),
                                                    "events", dbStats.connectionEvents()),
                                    "health", Map.of("healthy", dbStats.healthy())),
                    "system", Map.of("timestamp", System.currentTimeMillis()));
        });
    }

    private double calculateHealthScore(AppMetrics.CacheStats cacheStats, AppMetrics.DatabaseStats dbStats) {
        // Simple health calculation
        double cacheHealth = cacheStats.overallHitRate() / 100.0;
        double dbHealth = dbStats.healthy() ? 1.0 : 0.0;

        return ((cacheHealth * 0.4) + (dbHealth * 0.6)) * 100.0;
    }

    private String determineHealthStatus(double healthScore) {
        if (healthScore >= 90) return "EXCELLENT";
        if (healthScore >= 75) return "GOOD";
        if (healthScore >= 60) return "FAIR";
        if (healthScore >= 40) return "POOR";
        return "CRITICAL";
    }

    /**
     * Combined system health metrics
     */
    public record SystemHealthMetrics(
            AppMetrics.CacheStats cacheMetrics,
            AppMetrics.DatabaseStats databaseMetrics,
            double healthScore,
            String healthStatus,
            long timestamp) {}
}
