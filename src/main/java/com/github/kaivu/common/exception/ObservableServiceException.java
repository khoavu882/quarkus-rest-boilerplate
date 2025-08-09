package com.github.kaivu.common.exception;

import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.utils.ObservabilityUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.util.Map;

/**
 * Enhanced ServiceException that includes comprehensive observability context for error correlation.
 * Extends the existing ServiceException to maintain backward compatibility while adding:
 * - Full observability context including trace and span information
 * - Tenant isolation context for multi-tenant environments
 * - Error classification and correlation capabilities
 * - Integration with monitoring and alerting systems
 * - Structured error context for debugging and analysis
 */
@Slf4j
@Getter
public class ObservableServiceException extends ServiceException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, String> observabilityContext;
    private final String errorClassification;
    private final String tenantContext;
    private final long errorTimestamp;
    private final String errorId;

    public ObservableServiceException(AppErrorEnum errorsEnum, ObservabilityContext context) {
        super(errorsEnum);
        this.observabilityContext = context != null ? context.getErrorContext() : Map.of();
        this.errorClassification = classifyError(errorsEnum);
        this.tenantContext = extractTenantContext(context);
        this.errorTimestamp = System.currentTimeMillis();
        this.errorId = generateErrorId();

        // Record error in observability systems
        recordErrorObservability(errorsEnum, context, null);
    }

    public ObservableServiceException(
            String entityName, String errorKey, String message, AppErrorEnum errorsEnum, ObservabilityContext context) {
        super(entityName, errorKey, message, errorsEnum);
        this.observabilityContext = context != null ? context.getErrorContext() : Map.of();
        this.errorClassification = classifyError(errorsEnum);
        this.tenantContext = extractTenantContext(context);
        this.errorTimestamp = System.currentTimeMillis();
        this.errorId = generateErrorId();

        // Record error in observability systems
        recordErrorObservability(errorsEnum, context, String.format("%s.%s", entityName, errorKey));
    }

    /**
     * Create an ObservableServiceException with a root cause
     */
    public ObservableServiceException(AppErrorEnum errorsEnum, ObservabilityContext context, Throwable cause) {
        super(errorsEnum);
        this.observabilityContext = context != null ? context.getErrorContext() : Map.of();
        this.errorClassification = classifyError(errorsEnum, cause);
        this.tenantContext = extractTenantContext(context);
        this.errorTimestamp = System.currentTimeMillis();
        this.errorId = generateErrorId();

        // Record error in observability systems with cause
        recordErrorObservability(errorsEnum, context, null, cause);
    }

    /**
     * Create from existing ServiceException with observability context
     */
    public static ObservableServiceException from(ServiceException serviceException, ObservabilityContext context) {
        return new ObservableServiceException(
                serviceException.getEntityName(),
                serviceException.getErrorKey(),
                serviceException.getMessage(),
                serviceException.getErrorsEnum(),
                context);
    }

    /**
     * Get correlation ID from observability context
     */
    public String getCorrelationId() {
        return observabilityContext.get("correlationId");
    }

    /**
     * Get trace ID from observability context
     */
    public String getTraceId() {
        return observabilityContext.get("traceId");
    }

    /**
     * Get tenant ID from observability context
     */
    public String getTenantId() {
        return observabilityContext.get("tenantId");
    }

    /**
     * Get user ID from observability context
     */
    public String getUserId() {
        return observabilityContext.get("userId");
    }

    /**
     * Get request path from observability context
     */
    public String getRequestPath() {
        return observabilityContext.get("requestPath");
    }

    /**
     * Get HTTP method from observability context
     */
    public String getHttpMethod() {
        return observabilityContext.get("httpMethod");
    }

    @Override
    public String toString() {
        return String.format(
                "ObservableServiceException{errorId='%s', classification='%s', tenantContext='%s', message='%s', context=%s}",
                errorId, errorClassification, tenantContext, getMessage(), getContextSummary());
    }

    /**
     * Get error classification (business, technical, security, etc.)
     */
    public String getErrorClassification() {
        return errorClassification;
    }

    /**
     * Get tenant context summary
     */
    public String getTenantContext() {
        return tenantContext;
    }

    /**
     * Get error timestamp
     */
    public long getErrorTimestamp() {
        return errorTimestamp;
    }

    /**
     * Get unique error ID for correlation
     */
    public String getErrorId() {
        return errorId;
    }

    /**
     * Get full error context summary for logging
     */
    public String getContextSummary() {
        if (observabilityContext.isEmpty()) {
            return String.format(
                    "errorId=%s, classification=%s, timestamp=%d", errorId, errorClassification, errorTimestamp);
        }
        return String.format(
                "errorId=%s, correlationId=%s, traceId=%s, tenantId=%s, userId=%s, path=%s, method=%s, classification=%s, timestamp=%d",
                errorId,
                observabilityContext.get("correlationId"),
                observabilityContext.get("traceId"),
                observabilityContext.get("tenantId"),
                observabilityContext.get("userId"),
                observabilityContext.get("requestPath"),
                observabilityContext.get("httpMethod"),
                errorClassification,
                errorTimestamp);
    }

    /**
     * Get structured error details for monitoring systems
     */
    public Map<String, Object> getStructuredErrorDetails() {
        Map<String, Object> details = new java.util.HashMap<>(observabilityContext);
        details.put("errorId", errorId);
        details.put("errorClassification", errorClassification);
        details.put("errorTimestamp", errorTimestamp);
        details.put("tenantContext", tenantContext);
        details.put("errorType", this.getClass().getSimpleName());
        details.put("errorMessage", getMessage());

        if (getCause() != null) {
            details.put("rootCauseType", getCause().getClass().getSimpleName());
            details.put("rootCauseMessage", getCause().getMessage());
        }

        return details;
    }

    /**
     * Check if error should trigger alerts
     */
    public boolean shouldTriggerAlert() {
        // Trigger alerts for critical errors, security issues, or repeated tenant errors
        return errorClassification.equals("critical")
                || errorClassification.equals("security")
                || (tenantContext != null && errorClassification.equals("business"));
    }

    /**
     * Get error severity level
     */
    public String getSeverityLevel() {
        return switch (errorClassification) {
            case "critical", "security" -> "ERROR";
            case "technical", "integration" -> "WARN";
            case "business", "validation" -> "INFO";
            default -> "DEBUG";
        };
    }

    /**
     * Classify error based on error enum and optional cause
     */
    private String classifyError(AppErrorEnum errorsEnum, Throwable cause) {
        // Enhanced classification with cause analysis
        if (cause != null) {
            String causeType = cause.getClass().getSimpleName().toLowerCase();
            if (causeType.contains("security") || causeType.contains("auth")) {
                return "security";
            }
            if (causeType.contains("sql") || causeType.contains("database") || causeType.contains("connection")) {
                return "technical";
            }
            if (causeType.contains("timeout") || causeType.contains("circuit")) {
                return "integration";
            }
        }

        return classifyError(errorsEnum);
    }

    /**
     * Classify error based on error enum
     */
    private String classifyError(AppErrorEnum errorsEnum) {
        if (errorsEnum == null) {
            return "unknown";
        }

        String errorMessage = errorsEnum.getMessage().toLowerCase();
        String errorKey = errorsEnum.getErrorKey().toLowerCase();

        // Classification logic based on error patterns
        if (errorKey.contains("not_found") || errorKey.contains("missing")) {
            return "business";
        }
        if (errorKey.contains("unauthorized") || errorKey.contains("forbidden") || errorKey.contains("security")) {
            return "security";
        }
        if (errorKey.contains("validation") || errorKey.contains("invalid") || errorKey.contains("constraint")) {
            return "validation";
        }
        if (errorKey.contains("timeout") || errorKey.contains("circuit") || errorKey.contains("connection")) {
            return "integration";
        }
        if (errorKey.contains("critical") || errorKey.contains("fatal") || errorKey.contains("system")) {
            return "critical";
        }

        return "technical";
    }

    /**
     * Extract tenant context information
     */
    private String extractTenantContext(ObservabilityContext context) {
        if (context == null) {
            return "unknown";
        }

        String tenantId = context.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            return String.format("tenant=%s", tenantId);
        }

        return "single-tenant";
    }

    /**
     * Generate unique error ID for correlation
     */
    private String generateErrorId() {
        return String.format(
                "err-%d-%s",
                System.currentTimeMillis() % 1000000,
                java.util.UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Record error in observability systems
     */
    private void recordErrorObservability(AppErrorEnum errorsEnum, ObservabilityContext context, String errorKey) {
        recordErrorObservability(errorsEnum, context, errorKey, null);
    }

    /**
     * Record error in observability systems with optional cause
     */
    private void recordErrorObservability(
            AppErrorEnum errorsEnum, ObservabilityContext context, String errorKey, Throwable cause) {
        try {
            // Record error in current span
            ObservabilityUtil.recordError(cause != null ? cause : this);

            // Add error-specific span attributes
            ObservabilityUtil.addSpanAttribute("error.id", errorId);
            ObservabilityUtil.addSpanAttribute("error.classification", errorClassification);
            ObservabilityUtil.addSpanAttribute("error.tenant", tenantContext);
            ObservabilityUtil.addSpanAttribute("error.timestamp", String.valueOf(errorTimestamp));

            if (errorKey != null) {
                ObservabilityUtil.addSpanAttribute("error.key", errorKey);
            }
            if (errorsEnum != null) {
                ObservabilityUtil.addSpanAttribute("error.enum", errorsEnum.getErrorKey());
            }

        } catch (Exception e) {
            // Don't let observability recording fail the main operation
            log.error("Failed to record error observability: {}", e.getMessage());
        }
    }
}
