package com.github.kaivu.config;

import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.annotations.LogExecutionTime;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Interceptor
@LogExecutionTime
@Priority(Interceptor.Priority.APPLICATION)
public class LogExecutionTimeInterceptor {

    @Inject
    ObservabilityContext observabilityContext;

    @AroundInvoke
    public Object logExecutionTime(final InvocationContext context) throws Exception {
        final String methodName = context.getMethod().getDeclaringClass().getSimpleName() + "."
                + context.getMethod().getName();
        final Instant start = Instant.now();

        final Object result = context.proceed();

        if (result instanceof Uni<?>) {
            return ((Uni<?>) result)
                    .onItem()
                    .invoke(item -> logDurationWithContext(methodName, start, false, null))
                    .onFailure()
                    .invoke(failure -> {
                        logDurationWithContext(methodName, start, true, failure);
                        ObservabilityUtil.recordError(failure);
                    });
        } else {
            logDurationWithContext(methodName, start, false, null);
            return result;
        }
    }

    private void logDurationWithContext(
            final String methodName, final Instant start, final boolean failed, Throwable exception) {
        final Duration duration = Duration.between(start, Instant.now());
        final String contextSummary = observabilityContext.getContextSummary();

        if (failed) {
            log.info(
                    "Method {} failed after: {} ms with exception {} [context: {}]",
                    methodName,
                    duration.toMillis(),
                    exception != null ? exception.getMessage() : "unknown",
                    contextSummary);
        } else {
            log.info("Method {} execution time: {} ms [context: {}]", methodName, duration.toMillis(), contextSummary);
        }
    }

    // Legacy method maintained for backward compatibility
    @Deprecated(since = "1.0.0", forRemoval = false)
    private void logDuration(final String methodName, final Instant start, final boolean failed, Throwable exception) {
        final Duration duration = Duration.between(start, Instant.now());
        if (failed) {
            log.info(
                    "Method {} failed after: {} ms with exception {}",
                    methodName,
                    duration.toMillis(),
                    exception.getMessage());
        } else {
            log.info("Method {} execution time: {} ms", methodName, duration.toMillis());
        }
    }
}
