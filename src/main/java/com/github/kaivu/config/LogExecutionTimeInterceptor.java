package com.github.kaivu.config;

import com.github.kaivu.config.annotations.LogExecutionTime;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
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

    @AroundInvoke
    public Object logExecutionTime(final InvocationContext context) throws Exception {
        final String methodName = context.getMethod().getDeclaringClass().getSimpleName() + "."
                + context.getMethod().getName();
        final Instant start = Instant.now();

        final Object result = context.proceed();

        if (result instanceof Uni<?>) {
            return ((Uni<?>) result)
                    .onItem()
                    .invoke(item -> logDuration(methodName, start, false, null))
                    .onFailure()
                    .invoke(failure -> logDuration(methodName, start, true, failure));
        } else {
            logDuration(methodName, start, false, null);
            return result;
        }
    }

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
