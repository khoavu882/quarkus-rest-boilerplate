package com.github.kaivu.common.constant;

/**
 * Constants for observability headers, MDC keys, and metric names
 */
public final class ObservabilityConstant {

    // HTTP Headers
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String TENANT_ID_HEADER = "X-Tenant-ID";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String SESSION_ID_HEADER = "X-Session-ID";

    // MDC Keys for logging
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";
    public static final String MDC_TENANT_ID = "tenantId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_REQUEST_PATH = "requestPath";
    public static final String MDC_HTTP_METHOD = "httpMethod";

    // Metric Names
    public static final String METRIC_HTTP_REQUESTS_TOTAL = "http_requests_total";
    public static final String METRIC_HTTP_REQUEST_DURATION = "http_request_duration_seconds";
    public static final String METRIC_SERVICE_OPERATION_DURATION = "service_operation_duration_seconds";
    public static final String METRIC_SERVICE_OPERATION_ERRORS = "service_operation_errors_total";
    public static final String METRIC_DATABASE_OPERATION_DURATION = "database_operation_duration_seconds";
    public static final String METRIC_CACHE_OPERATION_DURATION = "cache_operation_duration_seconds";

    // Metric Tags
    public static final String TAG_METHOD = "method";
    public static final String TAG_PATH = "path";
    public static final String TAG_STATUS = "status";
    public static final String TAG_TENANT = "tenant";
    public static final String TAG_OPERATION = "operation";
    public static final String TAG_ERROR_TYPE = "error_type";
    public static final String TAG_SERVICE = "service";
    public static final String TAG_CACHE_TYPE = "cache_type";
    public static final String TAG_CACHE_RESULT = "result";

    // OpenTelemetry Attribute Keys
    public static final String OTEL_TENANT_ID = "app.tenant.id";
    public static final String OTEL_USER_ID = "app.user.id";
    public static final String OTEL_CORRELATION_ID = "app.correlation.id";
    public static final String OTEL_OPERATION_NAME = "app.operation.name";
    public static final String OTEL_SERVICE_LAYER = "app.service.layer";
    public static final String OTEL_ERROR_TYPE = "app.error.type";

    // Service Layer Values
    public static final String LAYER_CONTROLLER = "controller";
    public static final String LAYER_USECASE = "usecase";
    public static final String LAYER_SERVICE = "service";
    public static final String LAYER_REPOSITORY = "repository";
    public static final String LAYER_CLIENT = "client";

    // Cache Types
    public static final String CACHE_REDIS = "redis";
    public static final String CACHE_CAFFEINE = "caffeine";

    // Cache Results
    public static final String CACHE_HIT = "hit";
    public static final String CACHE_MISS = "miss";
    public static final String CACHE_ERROR = "error";

    private ObservabilityConstant() {}
}
