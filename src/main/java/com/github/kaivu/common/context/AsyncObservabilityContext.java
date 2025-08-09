package com.github.kaivu.common.context;

import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.utils.ObservabilityUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Asynchronous observability context that ensures proper propagation of
 * observability metadata across reactive and asynchronous operations.
 *
 * <p>This context leverages Quarkus SmallRye Context Propagation and Mutiny's
 * reactive operators to maintain context across thread boundaries in reactive
 * applications. It uses Mutiny's {@code emitOn} method with proper OpenTelemetry
 * context scope management instead of the non-existent {@code ContextPropagationOperator}.
 *
 * <p>This context handles:
 * <ul>
 *   <li>OpenTelemetry context propagation with proper {@code Scope} management</li>
 *   <li>MDC (Mapped Diagnostic Context) propagation for structured logging</li>
 *   <li>Tenant isolation context propagation</li>
 *   <li>Correlation ID propagation</li>
 *   <li>User and session context propagation</li>
 * </ul>
 *
 * <p>Usage patterns:
 * <ul>
 *   <li>{@code withContext(Uni<T>)} - Basic context propagation for reactive operations</li>
 *   <li>{@code wrapWithObservability(Uni<T>, String, String)} - Full observability with metrics and tracing</li>
 *   <li>{@code withContext(CompletableFuture<T>)} - Context propagation for standard Java futures</li>
 * </ul>
 */
@Slf4j
@ApplicationScoped
public class AsyncObservabilityContext {

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    TenantObservabilityContext tenantObservabilityContext;

    /**
     * Context snapshot for propagation across async boundaries
     */
    public static class ContextSnapshot {
        private final String correlationId;
        private final String traceId;
        private final String spanId;
        private final String tenantId;
        private final String userId;
        private final String sessionId;
        private final String requestPath;
        private final String httpMethod;
        private final Map<String, String> mdcContext;
        private final Context openTelemetryContext;

        private ContextSnapshot(ObservabilityContext observabilityContext, TenantObservabilityContext tenantContext) {
            this.correlationId = observabilityContext.getCorrelationId();
            this.traceId = observabilityContext.getTraceId();
            this.spanId = observabilityContext.getSpanId();
            this.tenantId = observabilityContext.getTenantId();
            this.userId = observabilityContext.getUserId();
            this.sessionId = observabilityContext.getSessionId();
            this.requestPath = observabilityContext.getRequestPath();
            this.httpMethod = observabilityContext.getHttpMethod();
            this.mdcContext = MDC.getCopyOfContextMap();
            this.openTelemetryContext = Context.current();
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public String getTraceId() {
            return traceId;
        }

        public String getSpanId() {
            return spanId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getUserId() {
            return userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getRequestPath() {
            return requestPath;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public Map<String, String> getMdcContext() {
            return mdcContext;
        }

        public Context getOpenTelemetryContext() {
            return openTelemetryContext;
        }
    }

    /**
     * Capture current observability context for async propagation
     */
    public ContextSnapshot captureContext() {
        return new ContextSnapshot(observabilityContext, tenantObservabilityContext);
    }

    /**
     * Restore context from snapshot in async operation
     */
    public void restoreContext(ContextSnapshot snapshot) {
        if (snapshot == null) {
            log.debug("No context snapshot to restore");
            return;
        }

        try {
            // Restore ObservabilityContext (if within same request scope)
            if (observabilityContext != null) {
                observabilityContext.setCorrelationId(snapshot.getCorrelationId());
                observabilityContext.setTraceId(snapshot.getTraceId());
                observabilityContext.setSpanId(snapshot.getSpanId());
                observabilityContext.setTenantId(snapshot.getTenantId());
                observabilityContext.setUserId(snapshot.getUserId());
                observabilityContext.setSessionId(snapshot.getSessionId());
                observabilityContext.setRequestPath(snapshot.getRequestPath());
                observabilityContext.setHttpMethod(snapshot.getHttpMethod());
            }

            // Restore MDC context
            if (snapshot.getMdcContext() != null) {
                MDC.setContextMap(snapshot.getMdcContext());
            }

            log.debug(
                    "Context restored for async operation: correlationId={}, tenantId={}",
                    snapshot.getCorrelationId(),
                    snapshot.getTenantId());

        } catch (Exception e) {
            log.warn("Error restoring observability context: {}", e.getMessage(), e);
        }
    }

    /**
     * Execute a Uni operation with context propagation.
     * Uses Mutiny's emitOn with proper OpenTelemetry context scope management
     * and MDC propagation for reactive operations in Quarkus.
     */
    public <T> Uni<T> withContext(Uni<T> uni) {
        ContextSnapshot snapshot = captureContext();

        return uni.emitOn(runnable -> {
                    // Restore OpenTelemetry context with proper scope management
                    try (Scope scope = snapshot.getOpenTelemetryContext() != null
                            ? snapshot.getOpenTelemetryContext().makeCurrent()
                            : null) {
                        restoreContext(snapshot);
                        runnable.run();
                    } finally {
                        clearContext();
                    }
                })
                .onItem()
                .invoke(item -> log.trace("Uni operation completed with context: {}", snapshot.getCorrelationId()))
                .onFailure()
                .invoke(failure -> log.debug(
                        "Uni operation failed with context: {}, error: {}",
                        snapshot.getCorrelationId(),
                        failure.getMessage()));
    }

    /**
     * Execute a CompletableFuture with context propagation
     */
    public <T> CompletableFuture<T> withContext(CompletableFuture<T> future) {
        ContextSnapshot snapshot = captureContext();

        return future.whenComplete((result, throwable) -> {
            restoreContext(snapshot);
            try {
                if (throwable != null) {
                    log.debug(
                            "CompletableFuture failed with context: {}, error: {}",
                            snapshot.getCorrelationId(),
                            throwable.getMessage());

                    // Record error in current span if available
                    ObservabilityUtil.recordError(throwable);
                } else {
                    log.trace("CompletableFuture completed with context: {}", snapshot.getCorrelationId());
                }
            } finally {
                clearContext();
            }
        });
    }

    /**
     * Execute a Supplier with context propagation
     */
    public <T> Supplier<T> withContext(Supplier<T> supplier) {
        ContextSnapshot snapshot = captureContext();

        return () -> {
            restoreContext(snapshot);
            try {
                T result = supplier.get();
                log.trace("Supplier executed with context: {}", snapshot.getCorrelationId());
                return result;
            } catch (Exception e) {
                log.debug("Supplier failed with context: {}, error: {}", snapshot.getCorrelationId(), e.getMessage());

                // Record error in current span if available
                ObservabilityUtil.recordError(e);
                throw e;
            } finally {
                clearContext();
            }
        };
    }

    /**
     * Execute a Runnable with context propagation
     */
    public Runnable withContext(Runnable runnable) {
        ContextSnapshot snapshot = captureContext();

        return () -> {
            restoreContext(snapshot);
            try {
                runnable.run();
                log.trace("Runnable executed with context: {}", snapshot.getCorrelationId());
            } catch (Exception e) {
                log.debug("Runnable failed with context: {}, error: {}", snapshot.getCorrelationId(), e.getMessage());

                // Record error in current span if available
                ObservabilityUtil.recordError(e);
                throw e;
            } finally {
                clearContext();
            }
        };
    }

    /**
     * Create a context-aware executor that propagates observability context
     */
    public Executor contextAwareExecutor(Executor delegate) {
        return command -> {
            ContextSnapshot snapshot = captureContext();
            delegate.execute(() -> {
                restoreContext(snapshot);
                try {
                    command.run();
                } finally {
                    clearContext();
                }
            });
        };
    }

    /**
     * Clear current thread's context
     */
    public void clearContext() {
        try {
            ObservabilityUtil.clearMDCContext();
            log.trace("Thread context cleared");
        } catch (Exception e) {
            log.warn("Error clearing observability context: {}", e.getMessage());
        }
    }

    /**
     * Create a new span with context propagation for async operations
     */
    public Span createAsyncSpan(String operationName, String serviceLayer) {
        ContextSnapshot snapshot = captureContext();

        Span span = io.opentelemetry.api.GlobalOpenTelemetry.getTracer("async-operations")
                .spanBuilder(operationName)
                .setAttribute(ObservabilityConstant.OTEL_OPERATION_NAME, operationName)
                .setAttribute(ObservabilityConstant.OTEL_SERVICE_LAYER, serviceLayer)
                .startSpan();

        // Enrich span with captured context
        if (snapshot.getCorrelationId() != null) {
            span.setAttribute(ObservabilityConstant.OTEL_CORRELATION_ID, snapshot.getCorrelationId());
        }
        if (snapshot.getTenantId() != null) {
            span.setAttribute(ObservabilityConstant.OTEL_TENANT_ID, snapshot.getTenantId());
        }
        if (snapshot.getUserId() != null) {
            span.setAttribute(ObservabilityConstant.OTEL_USER_ID, snapshot.getUserId());
        }

        return span;
    }

    /**
     * Wrap a Uni with comprehensive async observability.
     * Uses Mutiny's emitOn with proper OpenTelemetry context scope management
     * for tracing, metrics, and structured logging in reactive operations.
     */
    public <T> Uni<T> wrapWithObservability(Uni<T> uni, String operationName, String serviceLayer) {
        ContextSnapshot snapshot = captureContext();
        Span span = createAsyncSpan(operationName, serviceLayer);

        return uni.emitOn(runnable -> {
                    // Restore OpenTelemetry context with proper scope management
                    try (Scope scope = snapshot.getOpenTelemetryContext() != null
                            ? snapshot.getOpenTelemetryContext().makeCurrent()
                            : null) {
                        restoreContext(snapshot);
                        runnable.run();
                    } finally {
                        clearContext();
                    }
                })
                .onItem()
                .invoke(item -> {
                    span.setAttribute("success", true);
                    span.end();
                    log.debug(
                            "Async Uni operation completed: {} [context={}]",
                            operationName,
                            snapshot.getCorrelationId());
                })
                .onFailure()
                .invoke(failure -> {
                    span.recordException(failure);
                    span.setAttribute("error", true);
                    span.setAttribute("error.type", failure.getClass().getSimpleName());
                    span.end();
                    log.error(
                            "Async Uni operation failed: {} [context={}, error={}]",
                            operationName,
                            snapshot.getCorrelationId(),
                            failure.getMessage(),
                            failure);
                });
    }

    /**
     * Get current context summary for debugging
     */
    public String getCurrentContextSummary() {
        ContextSnapshot snapshot = captureContext();
        return String.format(
                "correlationId=%s, traceId=%s, tenantId=%s, userId=%s",
                snapshot.getCorrelationId(), snapshot.getTraceId(), snapshot.getTenantId(), snapshot.getUserId());
    }

    /**
     * Check if valid observability context exists
     */
    public boolean hasValidContext() {
        try {
            ContextSnapshot snapshot = captureContext();
            return snapshot.getCorrelationId() != null || snapshot.getTraceId() != null;
        } catch (Exception e) {
            log.debug("Error checking context validity: {}", e.getMessage());
            return false;
        }
    }
}
