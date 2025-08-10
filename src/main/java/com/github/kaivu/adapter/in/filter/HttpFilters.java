package com.github.kaivu.adapter.in.filter;

import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.context.TenantObservabilityContext;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.ApplicationConfiguration;
import com.github.kaivu.config.metrics.AppMetrics;
import com.github.kaivu.domain.audit.AuditListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Provider
@ApplicationScoped
public class HttpFilters implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String MANAGEMENT_PREFIX_PATH = "/q/";
    private static final String GZIP_ENCODING = "gzip";
    private static final String REQUEST_START_TIME = "X-StartTime";

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    TenantObservabilityContext tenantObservabilityContext;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    AppMetrics appMetrics;

    @Inject
    ApplicationConfiguration config;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        // Skip management endpoints
        if (path.contains(MANAGEMENT_PREFIX_PATH)) {
            return;
        }

        // Store the request start time
        Instant startTime = Instant.now();
        requestContext.setProperty(REQUEST_START_TIME, startTime);

        // Initialize observability context
        initializeObservabilityContext(requestContext, startTime);

        // Start HTTP request timer
        Timer.Sample timerSample = Timer.start(meterRegistry);
        requestContext.setProperty("observability.timer.sample", timerSample);

        // Record request metrics
        recordRequestMetrics(requestContext);

        if (Boolean.TRUE.equals(config.http.authLogging)) {
            log.debug("Authentication logging enabled for request: {} {}", requestContext.getMethod(), path);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Skip management endpoints
        if (path.contains(MANAGEMENT_PREFIX_PATH)) {
            return;
        }

        try {
            // Record response metrics
            recordResponseMetrics(requestContext, responseContext);

            // Enhanced logging with observability context
            logHttpRequestWithObservability(requestContext, responseContext);

            // Handle compression
            handleCompression(requestContext, responseContext);

        } finally {
            // Clean up observability context
            cleanupObservabilityContext();
            AuditListener.clearCurrentUser();
        }
    }

    /**
     * Initialize observability context with request information
     */
    private void initializeObservabilityContext(ContainerRequestContext requestContext, Instant startTime) {
        String path = requestContext.getUriInfo().getPath();

        // Skip management endpoints
        if (path.contains(MANAGEMENT_PREFIX_PATH)) {
            return;
        }

        // Extract headers
        String correlationId = getHeaderValue(requestContext, ObservabilityConstant.CORRELATION_ID_HEADER);
        String tenantId = getHeaderValue(requestContext, ObservabilityConstant.TENANT_ID_HEADER);
        String userId = getHeaderValue(requestContext, ObservabilityConstant.USER_ID_HEADER);
        String sessionId = getHeaderValue(requestContext, ObservabilityConstant.SESSION_ID_HEADER);

        // Set observability context
        observabilityContext.setCorrelationId(
                correlationId != null ? correlationId : observabilityContext.getOrGenerateCorrelationId());
        observabilityContext.setTenantId(tenantId);
        observabilityContext.setUserId(userId);
        observabilityContext.setSessionId(sessionId);
        observabilityContext.setRequestPath(path);
        observabilityContext.setHttpMethod(requestContext.getMethod());
        observabilityContext.setUserAgent(getHeaderValue(requestContext, HttpHeaders.USER_AGENT));
        observabilityContext.setRemoteIp(getRemoteIpAddress(requestContext));
        observabilityContext.setRequestStartTime(startTime);

        // Get current trace information
        ObservabilityUtil.getCurrentTraceInfo().ifPresent(traceInfo -> {
            observabilityContext.setTraceId(traceInfo.traceId());
            observabilityContext.setSpanId(traceInfo.spanId());
        });

        // Set MDC for structured logging
        ObservabilityUtil.setMDCContext(
                observabilityContext.getCorrelationId(),
                observabilityContext.getTraceId(),
                observabilityContext.getSpanId(),
                observabilityContext.getTenantId(),
                observabilityContext.getUserId(),
                path,
                requestContext.getMethod());

        // Enrich current span with context
        ObservabilityUtil.enrichSpanWithContext(
                observabilityContext.getCorrelationId(),
                observabilityContext.getTenantId(),
                observabilityContext.getUserId(),
                String.format("HTTP %s %s", requestContext.getMethod(), path),
                ObservabilityConstant.LAYER_CONTROLLER);

        // Add response header for correlation tracking
        if (observabilityContext.getCorrelationId() != null) {
            requestContext.setProperty("observability.correlation.id", observabilityContext.getCorrelationId());
        }

        // Add tenant-specific span attributes
        tenantObservabilityContext.addTenantSpanAttributes();

        // Record tenant-specific request
        if (tenantObservabilityContext.isMultiTenantMode()) {
            appMetrics.recordRequest(tenantObservabilityContext.getCurrentTenant());
        }
    }

    /**
     * Enhanced HTTP request logging with observability context
     */
    private void logHttpRequestWithObservability(
            ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path.contains(MANAGEMENT_PREFIX_PATH)) {
            return;
        }

        // Calculate execution time
        Instant startTime = (Instant) requestContext.getProperty(REQUEST_START_TIME);
        Instant endTime = Instant.now();
        long executionTimeMs = ObservabilityUtil.calculateDurationMillis(startTime, endTime);

        // Add correlation ID to response header
        String correlationId = (String) requestContext.getProperty("observability.correlation.id");
        if (correlationId != null) {
            responseContext.getHeaders().add(ObservabilityConstant.CORRELATION_ID_HEADER, correlationId);
        }

        // Add tenant ID to response header for multi-tenant support
        if (tenantObservabilityContext.isMultiTenantMode()) {
            responseContext
                    .getHeaders()
                    .add(ObservabilityConstant.TENANT_ID_HEADER, tenantObservabilityContext.getCurrentTenant());
        }

        // Enhanced structured logging
        log.info(
                "(HTTP) method: {}, host: {}, path: {}, status: {}, locale: {}, execution time: {} ms, context: {}",
                requestContext.getMethod().toUpperCase(Locale.ROOT),
                requestContext.getUriInfo().getBaseUri().getHost(),
                path,
                responseContext.getStatus(),
                requestContext.getLanguage(),
                executionTimeMs,
                observabilityContext.getContextSummary());

        // Log additional context for debugging if enabled
        if (log.isDebugEnabled()) {
            log.debug(
                    "Request details - Remote IP: {}, User-Agent: {}, Tenant: {}, User: {}",
                    observabilityContext.getRemoteIp(),
                    observabilityContext.getUserAgent(),
                    observabilityContext.getTenantId(),
                    observabilityContext.getUserId());
        }
    }

    /**
     * Clean up observability context
     */
    private void cleanupObservabilityContext() {
        try {
            // Clear MDC context
            ObservabilityUtil.clearMDCContext();

            // ObservabilityContext will be automatically cleaned up by CDI request scope
            // TenantObservabilityContext will also be cleaned up by CDI request scope
        } catch (Exception e) {
            log.warn("Error during observability context cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Get header value safely
     */
    private String getHeaderValue(ContainerRequestContext requestContext, String headerName) {
        return requestContext.getHeaderString(headerName);
    }

    /**
     * Extract remote IP address considering proxy headers
     */
    private String getRemoteIpAddress(ContainerRequestContext requestContext) {
        // Check for proxy headers first
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = requestContext.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fallback to standard headers or request info
        return "unknown";
    }

    /**
     * Record comprehensive request metrics
     */
    private void recordRequestMetrics(ContainerRequestContext requestContext) {
        String path = sanitizePath(requestContext.getUriInfo().getPath());
        String method = requestContext.getMethod();
        String tenant = tenantObservabilityContext.getCurrentTenant();

        // Increment total HTTP requests counter
        Counter.builder(ObservabilityConstant.METRIC_HTTP_REQUESTS_TOTAL)
                .tag(ObservabilityConstant.TAG_METHOD, method)
                .tag(ObservabilityConstant.TAG_PATH, path)
                .tag(ObservabilityConstant.TAG_TENANT, ObservabilityUtil.sanitizeTagValue(tenant))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record comprehensive response metrics
     */
    private void recordResponseMetrics(
            ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String path = sanitizePath(requestContext.getUriInfo().getPath());
        String method = requestContext.getMethod();
        String status = String.valueOf(responseContext.getStatus());
        String tenant = tenantObservabilityContext.getCurrentTenant();

        // Calculate request duration and record HTTP request duration
        Timer.Sample timerSample = (Timer.Sample) requestContext.getProperty("observability.timer.sample");
        if (timerSample != null) {
            timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_HTTP_REQUEST_DURATION)
                    .tag(ObservabilityConstant.TAG_METHOD, method)
                    .tag(ObservabilityConstant.TAG_PATH, path)
                    .tag(ObservabilityConstant.TAG_STATUS, status)
                    .tag(ObservabilityConstant.TAG_TENANT, ObservabilityUtil.sanitizeTagValue(tenant))
                    .register(meterRegistry));
        }

        // Record error metrics for non-2xx responses
        if (responseContext.getStatus() >= 400) {
            String errorType = getErrorType(responseContext.getStatus());
            appMetrics.recordError(tenantObservabilityContext.getCurrentTenant(), errorType);

            // Add error attributes to current span
            ObservabilityUtil.addSpanAttribute("http.status_code", status);
            ObservabilityUtil.addSpanAttribute("error", "true");
            ObservabilityUtil.addSpanAttribute("error.type", errorType);
        }
    }

    /**
     * Sanitize path for metrics to avoid cardinality explosion
     */
    private String sanitizePath(String path) {
        // Replace UUIDs and other variable path segments with placeholders
        return path.replaceAll("/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "/{id}")
                .replaceAll("/\\d+", "/{id}")
                .replaceAll("/[^/]*\\.[^/]*$", "/{file}");
    }

    /**
     * Determine error type based on HTTP status code
     */
    private String getErrorType(int statusCode) {
        if (statusCode >= 400 && statusCode < 500) {
            return "client_error";
        } else if (statusCode >= 500) {
            return "server_error";
        }
        return "unknown_error";
    }

    private void handleCompression(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        if (Boolean.FALSE.equals(config.http.enableCompression)) {
            return;
        }

        String encoding = requestContext.getHeaderString(HttpHeaders.ACCEPT_ENCODING);
        if (encoding != null && encoding.contains(GZIP_ENCODING)) {
            responseContext.getHeaders().put(HttpHeaders.CONTENT_ENCODING, List.of(GZIP_ENCODING));
            OutputStream outputStream = responseContext.getEntityStream();
            responseContext.setEntityStream(new GZIPOutputStream(outputStream));
        }
    }
}
