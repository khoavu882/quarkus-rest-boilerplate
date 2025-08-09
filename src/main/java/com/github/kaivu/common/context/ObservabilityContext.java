package com.github.kaivu.common.context;

import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Request-scoped observability context that maintains correlation IDs, tenant information,
 * and other observability metadata throughout the request lifecycle.
 *
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 */
@Getter
@Setter
@RequestScoped
public class ObservabilityContext {

    private String correlationId;
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String tenantId;
    private String userId;
    private String sessionId;
    private String requestPath;
    private String httpMethod;
    private String userAgent;
    private String remoteIp;
    private Instant requestStartTime;
    private Map<String, String> customAttributes = new ConcurrentHashMap<>();

    /**
     * Get correlation ID, generating one if not present
     */
    public String getOrGenerateCorrelationId() {
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = generateCorrelationId();
        }
        return correlationId;
    }

    /**
     * Add custom attribute for observability context
     */
    public void addAttribute(String key, String value) {
        customAttributes.put(key, value);
    }

    /**
     * Get custom attribute value
     */
    public Optional<String> getAttribute(String key) {
        return Optional.ofNullable(customAttributes.get(key));
    }

    /**
     * Check if tenant context is available
     */
    public boolean hasTenantContext() {
        return tenantId != null && !tenantId.isEmpty();
    }

    /**
     * Check if user context is available
     */
    public boolean hasUserContext() {
        return userId != null && !userId.isEmpty();
    }

    /**
     * Create a context summary for logging
     */
    public String getContextSummary() {
        return String.format(
                "correlationId=%s, traceId=%s, tenantId=%s, userId=%s, path=%s",
                correlationId, traceId, tenantId, userId, requestPath);
    }

    /**
     * Generate a new correlation ID
     */
    private String generateCorrelationId() {
        return "corr-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Create error context for exception handling
     */
    public Map<String, String> getErrorContext() {
        Map<String, String> context = new ConcurrentHashMap<>();
        if (correlationId != null) context.put("correlationId", correlationId);
        if (traceId != null) context.put("traceId", traceId);
        if (spanId != null) context.put("spanId", spanId);
        if (tenantId != null) context.put("tenantId", tenantId);
        if (userId != null) context.put("userId", userId);
        if (requestPath != null) context.put("requestPath", requestPath);
        if (httpMethod != null) context.put("httpMethod", httpMethod);
        context.putAll(customAttributes);
        return context;
    }

    /**
     * Reset the context (useful for testing)
     */
    public void reset() {
        correlationId = null;
        traceId = null;
        spanId = null;
        parentSpanId = null;
        tenantId = null;
        userId = null;
        sessionId = null;
        requestPath = null;
        httpMethod = null;
        userAgent = null;
        remoteIp = null;
        requestStartTime = null;
        customAttributes.clear();
    }
}
