package com.github.kaivu.common.utils;

import com.github.kaivu.common.constant.ObservabilityConstant;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for observability operations including MDC management,
 * OpenTelemetry integration, and metrics helpers.
 */
public final class ObservabilityUtil {

    private ObservabilityUtil() {}

    /**
     * Set MDC context for structured logging
     */
    public static void setMDCContext(
            String correlationId,
            String traceId,
            String spanId,
            String tenantId,
            String userId,
            String requestPath,
            String httpMethod) {
        if (correlationId != null) MDC.put(ObservabilityConstant.MDC_CORRELATION_ID, correlationId);
        if (traceId != null) MDC.put(ObservabilityConstant.MDC_TRACE_ID, traceId);
        if (spanId != null) MDC.put(ObservabilityConstant.MDC_SPAN_ID, spanId);
        if (tenantId != null) MDC.put(ObservabilityConstant.MDC_TENANT_ID, tenantId);
        if (userId != null) MDC.put(ObservabilityConstant.MDC_USER_ID, userId);
        if (requestPath != null) MDC.put(ObservabilityConstant.MDC_REQUEST_PATH, requestPath);
        if (httpMethod != null) MDC.put(ObservabilityConstant.MDC_HTTP_METHOD, httpMethod);
    }

    /**
     * Clear all MDC context
     */
    public static void clearMDCContext() {
        MDC.remove(ObservabilityConstant.MDC_CORRELATION_ID);
        MDC.remove(ObservabilityConstant.MDC_TRACE_ID);
        MDC.remove(ObservabilityConstant.MDC_SPAN_ID);
        MDC.remove(ObservabilityConstant.MDC_TENANT_ID);
        MDC.remove(ObservabilityConstant.MDC_USER_ID);
        MDC.remove(ObservabilityConstant.MDC_REQUEST_PATH);
        MDC.remove(ObservabilityConstant.MDC_HTTP_METHOD);
    }

    /**
     * Get current trace information from OpenTelemetry
     */
    public static Optional<TraceInfo> getCurrentTraceInfo() {
        Span currentSpan = Span.current();
        if (currentSpan == null) {
            return Optional.empty();
        }

        SpanContext spanContext = currentSpan.getSpanContext();
        if (!spanContext.isValid()) {
            return Optional.empty();
        }

        return Optional.of(new TraceInfo(spanContext.getTraceId(), spanContext.getSpanId(), spanContext.isSampled()));
    }

    /**
     * Add observability attributes to current span
     */
    public static void addSpanAttributes(Map<String, String> attributes) {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            attributes.forEach(currentSpan::setAttribute);
        }
    }

    /**
     * Add single attribute to current span
     */
    public static void addSpanAttribute(String key, String value) {
        Span currentSpan = Span.current();
        if (currentSpan != null && value != null) {
            currentSpan.setAttribute(key, value);
        }
    }

    /**
     * Add observability context to span
     */
    public static void enrichSpanWithContext(
            String correlationId, String tenantId, String userId, String operationName, String serviceLayer) {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            if (correlationId != null)
                currentSpan.setAttribute(ObservabilityConstant.OTEL_CORRELATION_ID, correlationId);
            if (tenantId != null) currentSpan.setAttribute(ObservabilityConstant.OTEL_TENANT_ID, tenantId);
            if (userId != null) currentSpan.setAttribute(ObservabilityConstant.OTEL_USER_ID, userId);
            if (operationName != null)
                currentSpan.setAttribute(ObservabilityConstant.OTEL_OPERATION_NAME, operationName);
            if (serviceLayer != null) currentSpan.setAttribute(ObservabilityConstant.OTEL_SERVICE_LAYER, serviceLayer);
        }
    }

    /**
     * Record error in span
     */
    public static void recordError(Throwable error) {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            currentSpan.recordException(error);
            currentSpan.setAttribute(
                    ObservabilityConstant.OTEL_ERROR_TYPE, error.getClass().getSimpleName());
        }
    }

    /**
     * Calculate duration in seconds for metrics
     */
    public static double calculateDurationSeconds(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0.0;
        }
        return Duration.between(start, end).toNanos() / 1_000_000_000.0;
    }

    /**
     * Calculate duration in milliseconds for logging
     */
    public static long calculateDurationMillis(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toMillis();
    }

    /**
     * Generate operation name for metrics and tracing
     */
    public static String generateOperationName(String service, String method) {
        return String.format("%s.%s", service, method);
    }

    /**
     * Sanitize metric tag values (remove special characters)
     */
    public static String sanitizeTagValue(String value) {
        if (value == null) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    /**
     * Record representing trace information
     */
    public record TraceInfo(String traceId, String spanId, boolean sampled) {}
}
