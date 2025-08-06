package com.github.kaivu.common.service;

import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.context.TenantObservabilityContext;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.metrics.AppMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Centralized observability service providing reusable patterns for
 * metrics collection, tracing, and structured logging across the application.
 * Handles both reactive and non-reactive operations with context propagation.
 */
@Slf4j
@ApplicationScoped
@LookupIfProperty(name = "quarkus.micrometer.enabled", stringValue = "true")
public class ObservabilityService {

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    TenantObservabilityContext tenantObservabilityContext;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    AppMetrics appMetrics;

    @Inject
    Tracer tracer;

    /**
     * Execute a synchronous operation with comprehensive observability
     */
    public <T> T execute(String operationName, String serviceLayer, Callable<T> operation) {
        return executeWithTimer(operationName, serviceLayer, operation, true, true);
    }

    /**
     * Execute a synchronous operation with configurable observability
     */
    public <T> T executeWithTimer(
            String operationName,
            String serviceLayer,
            Callable<T> operation,
            boolean enableMetrics,
            boolean enableLogging) {
        Instant start = Instant.now();
        Timer.Sample timerSample = enableMetrics ? Timer.start(meterRegistry) : null;

        // Create manual span for better control
        Span span = tracer.spanBuilder(operationName)
                .setAttribute(ObservabilityConstant.OTEL_OPERATION_NAME, operationName)
                .setAttribute(ObservabilityConstant.OTEL_SERVICE_LAYER, serviceLayer)
                .startSpan();

        try {
            enrichSpanWithContext(span, operationName, serviceLayer);

            if (enableLogging) {
                log.debug("Starting operation: {} [layer={}]", operationName, serviceLayer);
            }

            T result = operation.call();

            recordSuccessMetrics(operationName, serviceLayer, start, timerSample, enableMetrics);

            if (enableLogging) {
                long duration = Duration.between(start, Instant.now()).toMillis();
                log.info(
                        "Operation completed: {} [layer={}, duration={}ms, context={}]",
                        operationName,
                        serviceLayer,
                        duration,
                        observabilityContext.getContextSummary());
            }

            return result;

        } catch (Exception e) {
            recordErrorMetrics(operationName, serviceLayer, start, timerSample, enableMetrics, e);
            span.recordException(e);

            if (enableLogging) {
                long duration = Duration.between(start, Instant.now()).toMillis();
                log.error(
                        "Operation failed: {} [layer={}, duration={}ms, error={}, context={}]",
                        operationName,
                        serviceLayer,
                        duration,
                        e.getMessage(),
                        observabilityContext.getContextSummary(),
                        e);
            }

            throw new RuntimeException("Operation failed: " + operationName, e);
        } finally {
            span.end();
        }
    }

    /**
     * Execute a reactive Uni operation with comprehensive observability
     */
    public <T> Uni<T> executeUni(String operationName, String serviceLayer, Uni<T> operation) {
        return executeUni(operationName, serviceLayer, operation, true, true);
    }

    /**
     * Execute a reactive Uni operation with configurable observability
     */
    public <T> Uni<T> executeUni(
            String operationName, String serviceLayer, Uni<T> operation, boolean enableMetrics, boolean enableLogging) {
        Instant start = Instant.now();
        Timer.Sample timerSample = enableMetrics ? Timer.start(meterRegistry) : null;

        // Create span
        Span span = tracer.spanBuilder(operationName)
                .setAttribute(ObservabilityConstant.OTEL_OPERATION_NAME, operationName)
                .setAttribute(ObservabilityConstant.OTEL_SERVICE_LAYER, serviceLayer)
                .startSpan();

        enrichSpanWithContext(span, operationName, serviceLayer);

        if (enableLogging) {
            log.debug("Starting reactive operation: {} [layer={}]", operationName, serviceLayer);
        }

        return operation
                .onItem()
                .invoke(item -> {
                    recordSuccessMetrics(operationName, serviceLayer, start, timerSample, enableMetrics);

                    if (enableLogging) {
                        long duration = Duration.between(start, Instant.now()).toMillis();
                        log.info(
                                "Reactive operation completed: {} [layer={}, duration={}ms, context={}]",
                                operationName,
                                serviceLayer,
                                duration,
                                observabilityContext.getContextSummary());
                    }

                    span.end();
                })
                .onFailure()
                .invoke(failure -> {
                    recordErrorMetrics(operationName, serviceLayer, start, timerSample, enableMetrics, failure);
                    span.recordException(failure);

                    if (enableLogging) {
                        long duration = Duration.between(start, Instant.now()).toMillis();
                        log.error(
                                "Reactive operation failed: {} [layer={}, duration={}ms, error={}, context={}]",
                                operationName,
                                serviceLayer,
                                duration,
                                failure.getMessage(),
                                observabilityContext.getContextSummary(),
                                failure);
                    }

                    span.end();
                });
    }

    /**
     * Execute a reactive Multi operation with comprehensive observability
     */
    public <T> Multi<T> executeMulti(String operationName, String serviceLayer, Multi<T> operation) {
        return executeMulti(operationName, serviceLayer, operation, true, true);
    }

    /**
     * Execute a reactive Multi operation with configurable observability
     */
    public <T> Multi<T> executeMulti(
            String operationName,
            String serviceLayer,
            Multi<T> operation,
            boolean enableMetrics,
            boolean enableLogging) {
        Instant start = Instant.now();
        Timer.Sample timerSample = enableMetrics ? Timer.start(meterRegistry) : null;

        Span span = tracer.spanBuilder(operationName)
                .setAttribute(ObservabilityConstant.OTEL_OPERATION_NAME, operationName)
                .setAttribute(ObservabilityConstant.OTEL_SERVICE_LAYER, serviceLayer)
                .startSpan();

        enrichSpanWithContext(span, operationName, serviceLayer);

        if (enableLogging) {
            log.debug("Starting reactive multi operation: {} [layer={}]", operationName, serviceLayer);
        }

        return operation
                .onCompletion()
                .invoke(() -> {
                    recordSuccessMetrics(operationName, serviceLayer, start, timerSample, enableMetrics);

                    if (enableLogging) {
                        long duration = Duration.between(start, Instant.now()).toMillis();
                        log.info(
                                "Reactive multi operation completed: {} [layer={}, duration={}ms, context={}]",
                                operationName,
                                serviceLayer,
                                duration,
                                observabilityContext.getContextSummary());
                    }

                    span.end();
                })
                .onFailure()
                .invoke(failure -> {
                    recordErrorMetrics(operationName, serviceLayer, start, timerSample, enableMetrics, failure);
                    span.recordException(failure);

                    if (enableLogging) {
                        long duration = Duration.between(start, Instant.now()).toMillis();
                        log.error(
                                "Reactive multi operation failed: {} [layer={}, duration={}ms, error={}, context={}]",
                                operationName,
                                serviceLayer,
                                duration,
                                failure.getMessage(),
                                observabilityContext.getContextSummary(),
                                failure);
                    }

                    span.end();
                });
    }

    /**
     * Create an operation builder for more complex observability configurations
     */
    public OperationBuilder operation(String operationName) {
        return new OperationBuilder(operationName);
    }

    /**
     * Record a custom metric with tenant context
     */
    public void recordCustomMetric(String metricName, String operationType, Map<String, String> additionalTags) {
        Counter.Builder builder = Counter.builder(metricName)
                .tag(ObservabilityConstant.TAG_OPERATION, operationType)
                .tag(
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(tenantObservabilityContext.getCurrentTenant()));

        if (additionalTags != null) {
            additionalTags.forEach(builder::tag);
        }

        builder.register(meterRegistry).increment();
    }

    /**
     * Record a timed custom metric
     */
    public void recordTimedMetric(
            String metricName, Duration duration, String operationType, Map<String, String> additionalTags) {
        Timer.Builder builder = Timer.builder(metricName)
                .tag(ObservabilityConstant.TAG_OPERATION, operationType)
                .tag(
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(tenantObservabilityContext.getCurrentTenant()));

        if (additionalTags != null) {
            additionalTags.forEach(builder::tag);
        }

        builder.register(meterRegistry).record(duration);
    }

    /**
     * Add custom attributes to current span
     */
    public void addSpanAttributes(Map<String, String> attributes) {
        ObservabilityUtil.addSpanAttributes(attributes);
    }

    /**
     * Create a child span for nested operations
     */
    public Span createChildSpan(String operationName, String serviceLayer) {
        return tracer.spanBuilder(operationName)
                .setAttribute(ObservabilityConstant.OTEL_OPERATION_NAME, operationName)
                .setAttribute(ObservabilityConstant.OTEL_SERVICE_LAYER, serviceLayer)
                .startSpan();
    }

    /**
     * Get current observability context summary
     */
    public String getContextSummary() {
        return observabilityContext.getContextSummary();
    }

    /**
     * Get tenant-specific context summary
     */
    public String getTenantContextSummary() {
        return tenantObservabilityContext.getTenantIsolationSummary();
    }

    private void enrichSpanWithContext(Span span, String operationName, String serviceLayer) {
        span.setAttribute(ObservabilityConstant.OTEL_CORRELATION_ID, observabilityContext.getCorrelationId());

        if (tenantObservabilityContext.isMultiTenantMode()) {
            span.setAttribute(ObservabilityConstant.OTEL_TENANT_ID, tenantObservabilityContext.getCurrentTenant());
        }

        if (observabilityContext.getUserId() != null) {
            span.setAttribute(ObservabilityConstant.OTEL_USER_ID, observabilityContext.getUserId());
        }
    }

    private void recordSuccessMetrics(
            String operationName, String serviceLayer, Instant start, Timer.Sample timerSample, boolean enableMetrics) {
        if (!enableMetrics) return;

        if (timerSample != null) {
            timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_SERVICE_OPERATION_DURATION)
                    .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                    .tag(ObservabilityConstant.TAG_SERVICE, serviceLayer)
                    .tag(ObservabilityConstant.TAG_STATUS, "success")
                    .tag(
                            ObservabilityConstant.TAG_TENANT,
                            ObservabilityUtil.sanitizeTagValue(tenantObservabilityContext.getCurrentTenant()))
                    .register(meterRegistry));
        }
    }

    private void recordErrorMetrics(
            String operationName,
            String serviceLayer,
            Instant start,
            Timer.Sample timerSample,
            boolean enableMetrics,
            Throwable error) {
        if (!enableMetrics) return;

        // Record error counter
        Counter.builder(ObservabilityConstant.METRIC_SERVICE_OPERATION_ERRORS)
                .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                .tag(ObservabilityConstant.TAG_SERVICE, serviceLayer)
                .tag(ObservabilityConstant.TAG_ERROR_TYPE, error.getClass().getSimpleName())
                .tag(
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(tenantObservabilityContext.getCurrentTenant()))
                .register(meterRegistry)
                .increment();

        // Record duration for failed operations
        if (timerSample != null) {
            timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_SERVICE_OPERATION_DURATION)
                    .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                    .tag(ObservabilityConstant.TAG_SERVICE, serviceLayer)
                    .tag(ObservabilityConstant.TAG_STATUS, "error")
                    .tag(
                            ObservabilityConstant.TAG_TENANT,
                            ObservabilityUtil.sanitizeTagValue(tenantObservabilityContext.getCurrentTenant()))
                    .register(meterRegistry));
        }

        // Record tenant-specific error
        appMetrics.recordError(
                tenantObservabilityContext.getCurrentTenant(), error.getClass().getSimpleName());
    }

    /**
     * Builder pattern for complex observability operations
     */
    public class OperationBuilder {
        private final String operationName;
        private String serviceLayer = "unknown";
        private boolean enableMetrics = true;
        private boolean enableLogging = true;
        private Map<String, String> customTags = Map.of();

        private OperationBuilder(String operationName) {
            this.operationName = operationName;
        }

        public OperationBuilder layer(String serviceLayer) {
            this.serviceLayer = serviceLayer;
            return this;
        }

        public OperationBuilder metrics(boolean enabled) {
            this.enableMetrics = enabled;
            return this;
        }

        public OperationBuilder logging(boolean enabled) {
            this.enableLogging = enabled;
            return this;
        }

        public OperationBuilder tags(Map<String, String> customTags) {
            this.customTags = customTags != null ? customTags : Map.of();
            return this;
        }

        public <T> T execute(Callable<T> operation) {
            return executeWithTimer(operationName, serviceLayer, operation, enableMetrics, enableLogging);
        }

        public <T> Uni<T> executeUni(Uni<T> operation) {
            return ObservabilityService.this.executeUni(
                    operationName, serviceLayer, operation, enableMetrics, enableLogging);
        }

        public <T> Multi<T> executeMulti(Multi<T> operation) {
            return ObservabilityService.this.executeMulti(
                    operationName, serviceLayer, operation, enableMetrics, enableLogging);
        }
    }
}
