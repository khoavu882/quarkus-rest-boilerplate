package com.github.kaivu.config;

import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.annotations.Observability;
import com.github.kaivu.config.metrics.AppMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;

/**
 * Comprehensive observability interceptor that provides metrics, tracing,
 * and structured logging with context propagation.
 */
@Slf4j
@Interceptor
@Observability
@Priority(Interceptor.Priority.APPLICATION + 10)
public class ObservabilityInterceptor {

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    AppMetrics appMetrics;

    @AroundInvoke
    public Object observeExecution(final InvocationContext context) throws Exception {
        final Method method = context.getMethod();
        final Observability annotation = getObservabilityAnnotation(method);
        final String operationName = buildOperationName(method, annotation);
        final String serviceLayer = getServiceLayer(method, annotation);
        final Instant start = Instant.now();

        // Create timer for metrics
        Timer.Sample timerSample = annotation.metrics() ? Timer.start(meterRegistry) : null;

        try {
            // Enrich span with operation context
            enrichSpanWithOperationContext(operationName, serviceLayer);

            // Log operation start
            if (annotation.logging()) {
                logOperationStart(operationName, serviceLayer);
            }

            final Object result = context.proceed();

            // Handle reactive types
            if (result instanceof Uni<?>) {
                return handleUniResult((Uni<?>) result, operationName, serviceLayer, start, timerSample, annotation);
            } else if (result instanceof Multi<?>) {
                return handleMultiResult(
                        (Multi<?>) result, operationName, serviceLayer, start, timerSample, annotation);
            } else {
                // Handle synchronous result
                handleSyncSuccess(operationName, serviceLayer, start, timerSample, annotation);
                return result;
            }
        } catch (Exception e) {
            handleError(e, operationName, serviceLayer, start, timerSample, annotation);
            throw e;
        }
    }

    private Observability getObservabilityAnnotation(Method method) {
        Observability annotation = method.getAnnotation(Observability.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(Observability.class);
        }
        return annotation != null ? annotation : createDefaultAnnotation();
    }

    private Observability createDefaultAnnotation() {
        return new Observability() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Observability.class;
            }

            @Override
            public String value() {
                return "";
            }

            @Override
            public String layer() {
                return "";
            }

            @Override
            public boolean metrics() {
                return true;
            }

            @Override
            public boolean logging() {
                return true;
            }
        };
    }

    private String buildOperationName(Method method, Observability annotation) {
        if (!annotation.value().isEmpty()) {
            return annotation.value();
        }
        return ObservabilityUtil.generateOperationName(
                method.getDeclaringClass().getSimpleName(), method.getName());
    }

    private String getServiceLayer(Method method, Observability annotation) {
        if (!annotation.layer().isEmpty()) {
            return annotation.layer();
        }

        // Infer layer from class name patterns
        String className = method.getDeclaringClass().getSimpleName().toLowerCase();
        if (className.contains("resource") || className.contains("controller")) {
            return ObservabilityConstant.LAYER_CONTROLLER;
        } else if (className.contains("usecase")) {
            return ObservabilityConstant.LAYER_USECASE;
        } else if (className.contains("service")) {
            return ObservabilityConstant.LAYER_SERVICE;
        } else if (className.contains("repository")) {
            return ObservabilityConstant.LAYER_REPOSITORY;
        } else if (className.contains("client")) {
            return ObservabilityConstant.LAYER_CLIENT;
        }
        return "unknown";
    }

    private void enrichSpanWithOperationContext(String operationName, String serviceLayer) {
        ObservabilityUtil.enrichSpanWithContext(
                observabilityContext.getCorrelationId(),
                observabilityContext.getTenantId(),
                observabilityContext.getUserId(),
                operationName,
                serviceLayer);
    }

    private void logOperationStart(String operationName, String serviceLayer) {
        log.debug("Operation started: {} [layer={}]", operationName, serviceLayer);
    }

    private Uni<?> handleUniResult(
            Uni<?> uni,
            String operationName,
            String serviceLayer,
            Instant start,
            Timer.Sample timerSample,
            Observability annotation) {
        return uni.onItem()
                .invoke(item -> handleSyncSuccess(operationName, serviceLayer, start, timerSample, annotation))
                .onFailure()
                .invoke(failure -> handleError(failure, operationName, serviceLayer, start, timerSample, annotation));
    }

    private Multi<?> handleMultiResult(
            Multi<?> multi,
            String operationName,
            String serviceLayer,
            Instant start,
            Timer.Sample timerSample,
            Observability annotation) {
        return multi.onCompletion()
                .invoke(() -> handleSyncSuccess(operationName, serviceLayer, start, timerSample, annotation))
                .onFailure()
                .invoke(failure -> handleError(failure, operationName, serviceLayer, start, timerSample, annotation));
    }

    private void handleSyncSuccess(
            String operationName,
            String serviceLayer,
            Instant start,
            Timer.Sample timerSample,
            Observability annotation) {
        Duration duration = Duration.between(start, Instant.now());

        // Record metrics
        if (annotation.metrics() && timerSample != null) {
            timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_SERVICE_OPERATION_DURATION)
                    .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                    .tag(ObservabilityConstant.TAG_SERVICE, serviceLayer)
                    .tag(
                            ObservabilityConstant.TAG_TENANT,
                            ObservabilityUtil.sanitizeTagValue(observabilityContext.getTenantId()))
                    .register(meterRegistry));
        }

        // Log completion
        if (annotation.logging()) {
            log.info(
                    "Operation completed: {} [layer={}, duration={}ms]",
                    operationName,
                    serviceLayer,
                    duration.toMillis());
        }
    }

    private void handleError(
            Throwable error,
            String operationName,
            String serviceLayer,
            Instant start,
            Timer.Sample timerSample,
            Observability annotation) {
        Duration duration = Duration.between(start, Instant.now());

        // Record error metrics
        if (annotation.metrics()) {
            Counter.builder(ObservabilityConstant.METRIC_SERVICE_OPERATION_ERRORS)
                    .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                    .tag(ObservabilityConstant.TAG_SERVICE, serviceLayer)
                    .tag(ObservabilityConstant.TAG_ERROR_TYPE, error.getClass().getSimpleName())
                    .tag(
                            ObservabilityConstant.TAG_TENANT,
                            ObservabilityUtil.sanitizeTagValue(observabilityContext.getTenantId()))
                    .register(meterRegistry)
                    .increment();

            // Also record duration for failed operations
            if (timerSample != null) {
                timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_SERVICE_OPERATION_DURATION)
                        .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                        .tag(ObservabilityConstant.TAG_SERVICE, serviceLayer)
                        .tag("status", "error")
                        .tag(
                                ObservabilityConstant.TAG_TENANT,
                                ObservabilityUtil.sanitizeTagValue(observabilityContext.getTenantId()))
                        .register(meterRegistry));
            }
        }

        // Record error in span
        ObservabilityUtil.recordError(error);

        // Log error
        if (annotation.logging()) {
            log.error(
                    "Operation failed: {} [layer={}, duration={}ms, error={}]",
                    operationName,
                    serviceLayer,
                    duration.toMillis(),
                    error.getMessage(),
                    error);
        }
    }
}
