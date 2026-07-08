package com.github.kaivu.common.context;

import com.github.kaivu.common.constant.ObservabilityConstant;
import io.micrometer.core.instrument.Tag;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * Tenant-aware observability context that provides tenant isolation
 * and tenant-specific observability operations.
 */
@Getter
@RequestScoped
public class TenantObservabilityContext {

    @Inject
    ObservabilityContext observabilityContext;

    /**
     * Get current tenant ID with fallback
     */
    public String getCurrentTenant() {
        return Optional.ofNullable(observabilityContext.getTenantId())
                .filter(tenantId -> !tenantId.isEmpty())
                .orElse("default");
    }

    /**
     * Check if multi-tenant mode is active
     */
    public boolean isMultiTenantMode() {
        return observabilityContext.getTenantId() != null
                && !observabilityContext.getTenantId().isEmpty();
    }

    /**
     * Get tenant-specific metric tags
     */
    public List<Tag> getTenantMetricTags() {
        return List.of(Tag.of(ObservabilityConstant.TAG_TENANT, sanitizeTenantId()));
    }

    /**
     * Get tenant-specific logging prefix
     */
    public String getTenantLogPrefix() {
        if (isMultiTenantMode()) {
            return String.format("[tenant=%s] ", getCurrentTenant());
        }
        return "";
    }

    /**
     * Get tenant-specific cache key prefix
     */
    public String getTenantCacheKeyPrefix(String baseKey) {
        if (isMultiTenantMode()) {
            return String.format("tenant:%s:%s", getCurrentTenant(), baseKey);
        }
        return baseKey;
    }

    /**
     * Get tenant-specific metric name
     */
    public String getTenantMetricName(String baseMetricName) {
        // Note: Metric names should not include tenant ID directly.
        // Tenant information should be in tags for better aggregation.
        return baseMetricName;
    }

    /**
     * Create tenant isolation summary for debugging
     */
    public String getTenantIsolationSummary() {
        if (isMultiTenantMode()) {
            return String.format(
                    "tenant=%s, user=%s, session=%s",
                    getCurrentTenant(), observabilityContext.getUserId(), observabilityContext.getSessionId());
        }
        return "single-tenant-mode";
    }

    /**
     * Get tenant-specific span attributes
     */
    public void addTenantSpanAttributes() {
        if (isMultiTenantMode()) {
            io.opentelemetry.api.trace.Span currentSpan = io.opentelemetry.api.trace.Span.current();
            if (currentSpan != null) {
                currentSpan.setAttribute(ObservabilityConstant.OTEL_TENANT_ID, getCurrentTenant());
                if (observabilityContext.getUserId() != null) {
                    currentSpan.setAttribute(ObservabilityConstant.OTEL_USER_ID, observabilityContext.getUserId());
                }
            }
        }
    }

    /**
     * Validate tenant access for observability data
     */
    public boolean canAccessTenantData(String requestedTenantId) {
        if (!isMultiTenantMode()) {
            // In single-tenant mode, allow access to any data
            return true;
        }

        String currentTenant = getCurrentTenant();

        // Basic tenant isolation - can only access own tenant data
        // In production, this might involve more complex authorization logic
        return currentTenant.equals(requestedTenantId);
    }

    /**
     * Create tenant-aware error context
     */
    public String getTenantErrorContext() {
        return String.format(
                "tenant=%s, correlationId=%s, traceId=%s",
                getCurrentTenant(), observabilityContext.getCorrelationId(), observabilityContext.getTraceId());
    }

    /**
     * Sanitize tenant ID for use in metrics and tags
     */
    private String sanitizeTenantId() {
        String tenantId = getCurrentTenant();
        return tenantId.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    /**
     * Get tenant-specific database schema or connection
     * This is a placeholder for tenant-specific database access patterns
     */
    public String getTenantDatabaseSchema() {
        if (isMultiTenantMode()) {
            return String.format("tenant_%s", sanitizeTenantId());
        }
        return "public"; // Default schema
    }

    /**
     * Create tenant-specific cache namespace
     */
    public String getTenantCacheNamespace() {
        if (isMultiTenantMode()) {
            return String.format("cache:tenant:%s", sanitizeTenantId());
        }
        return "cache:default";
    }
}
