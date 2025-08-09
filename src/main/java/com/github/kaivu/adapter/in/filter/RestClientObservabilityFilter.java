package com.github.kaivu.adapter.in.filter;

import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.utils.ObservabilityUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * REST client filter that adds observability context to outgoing requests
 * and measures client request performance.
 */
@Slf4j
@Provider
@ApplicationScoped
public class RestClientObservabilityFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final String REQUEST_START_TIME = "observability.request.start.time";
    private static final String TIMER_SAMPLE = "observability.timer.sample";

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    MeterRegistry meterRegistry;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // Add observability headers to outgoing requests
        addObservabilityHeaders(requestContext);

        // Start timing the request
        Instant startTime = Instant.now();
        Timer.Sample timerSample = Timer.start(meterRegistry);

        requestContext.setProperty(REQUEST_START_TIME, startTime);
        requestContext.setProperty(TIMER_SAMPLE, timerSample);

        // Log outgoing request
        logOutgoingRequest(requestContext);

        // Add span attributes for outgoing request
        enrichSpanForOutgoingRequest(requestContext);
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        try {
            // Measure and log response
            measureAndLogResponse(requestContext, responseContext);
        } catch (Exception e) {
            log.warn("Error processing client response observability: {}", e.getMessage(), e);
        }
    }

    private void addObservabilityHeaders(ClientRequestContext requestContext) {
        // Add correlation ID for request tracking
        String correlationId = observabilityContext.getCorrelationId();
        if (correlationId != null) {
            requestContext.getHeaders().add(ObservabilityConstant.CORRELATION_ID_HEADER, correlationId);
        }

        // Add tenant context if available
        String tenantId = observabilityContext.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            requestContext.getHeaders().add(ObservabilityConstant.TENANT_ID_HEADER, tenantId);
        }

        // Add user context if available
        String userId = observabilityContext.getUserId();
        if (userId != null && !userId.isEmpty()) {
            requestContext.getHeaders().add(ObservabilityConstant.USER_ID_HEADER, userId);
        }

        // Add session context if available
        String sessionId = observabilityContext.getSessionId();
        if (sessionId != null && !sessionId.isEmpty()) {
            requestContext.getHeaders().add(ObservabilityConstant.SESSION_ID_HEADER, sessionId);
        }

        // Add trace context headers for distributed tracing
        ObservabilityUtil.getCurrentTraceInfo().ifPresent(traceInfo -> {
            // OpenTelemetry trace propagation headers would be added automatically
            // by the OpenTelemetry instrumentation, but we can add custom headers if needed
            requestContext
                    .getHeaders()
                    .add("X-Trace-Context", String.format("trace=%s,span=%s", traceInfo.traceId(), traceInfo.spanId()));
        });
    }

    private void logOutgoingRequest(ClientRequestContext requestContext) {
        String method = requestContext.getMethod();
        String uri = requestContext.getUri().toString();
        String host = requestContext.getUri().getHost();

        log.debug(
                "Outgoing REST request: {} {} [host={}, correlationId={}, tenant={}]",
                method,
                uri,
                host,
                observabilityContext.getCorrelationId(),
                observabilityContext.getTenantId());
    }

    private void enrichSpanForOutgoingRequest(ClientRequestContext requestContext) {
        String method = requestContext.getMethod();
        String uri = requestContext.getUri().toString();
        String host = requestContext.getUri().getHost();

        ObservabilityUtil.enrichSpanWithContext(
                observabilityContext.getCorrelationId(),
                observabilityContext.getTenantId(),
                observabilityContext.getUserId(),
                String.format("HTTP %s %s", method, uri),
                ObservabilityConstant.LAYER_CLIENT);

        // Add HTTP client specific attributes
        ObservabilityUtil.addSpanAttribute("http.method", method);
        ObservabilityUtil.addSpanAttribute("http.url", uri);
        ObservabilityUtil.addSpanAttribute("http.host", host);
        ObservabilityUtil.addSpanAttribute("client.type", "rest");
    }

    private void measureAndLogResponse(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        Instant startTime = (Instant) requestContext.getProperty(REQUEST_START_TIME);
        Timer.Sample timerSample = (Timer.Sample) requestContext.getProperty(TIMER_SAMPLE);

        if (startTime == null || timerSample == null) {
            log.warn("Missing timing information for client request");
            return;
        }

        Duration duration = Duration.between(startTime, Instant.now());
        int statusCode = responseContext.getStatus();
        String method = requestContext.getMethod();
        String host = requestContext.getUri().getHost();
        String path = requestContext.getUri().getPath();

        // Record metrics
        timerSample.stop(Timer.builder("http_client_request_duration_seconds")
                .tag(ObservabilityConstant.TAG_METHOD, method.toLowerCase())
                .tag(ObservabilityConstant.TAG_STATUS, String.valueOf(statusCode))
                .tag("host", ObservabilityUtil.sanitizeTagValue(host))
                .tag(
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(observabilityContext.getTenantId()))
                .register(meterRegistry));

        // Record response status counter
        meterRegistry
                .counter(
                        "http_client_requests_total",
                        ObservabilityConstant.TAG_METHOD,
                        method.toLowerCase(),
                        ObservabilityConstant.TAG_STATUS,
                        String.valueOf(statusCode),
                        "host",
                        ObservabilityUtil.sanitizeTagValue(host),
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(observabilityContext.getTenantId()))
                .increment();

        // Add response attributes to span
        ObservabilityUtil.addSpanAttribute("http.status_code", String.valueOf(statusCode));
        ObservabilityUtil.addSpanAttribute("http.response.duration_ms", String.valueOf(duration.toMillis()));

        // Log response
        if (statusCode >= 400) {
            log.warn(
                    "REST client request failed: {} {} [host={}, status={}, duration={}ms, correlationId={}]",
                    method,
                    path,
                    host,
                    statusCode,
                    duration.toMillis(),
                    observabilityContext.getCorrelationId());
        } else {
            log.debug(
                    "REST client request completed: {} {} [host={}, status={}, duration={}ms, correlationId={}]",
                    method,
                    path,
                    host,
                    statusCode,
                    duration.toMillis(),
                    observabilityContext.getCorrelationId());
        }

        // Record error in span for client errors (4xx) and server errors (5xx)
        if (statusCode >= 400) {
            ObservabilityUtil.addSpanAttribute("error", "true");
            ObservabilityUtil.addSpanAttribute("error.type", statusCode >= 500 ? "server_error" : "client_error");
        }
    }
}
