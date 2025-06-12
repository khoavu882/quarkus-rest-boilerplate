package com.github.kaivu.web.config;

import com.github.kaivu.infrastructure.annotations.LogExecutionTime;
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
    public Object logExecutionTime(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getDeclaringClass().getSimpleName() + "."
                + context.getMethod().getName();
        Instant start = Instant.now();

        Object result = context.proceed();

        // If the result is a Uni, we need to measure time when it completes
        if (result instanceof Uni<?>) {
            return ((Uni<?>) result)
                    .onItem()
                    .invoke(item -> {
                        Duration duration = Duration.between(start, Instant.now());
                        log.info("Async method {} execution time: {} ms", methodName, duration.toMillis());
                    })
                    .onFailure()
                    .invoke(failure -> {
                        Duration duration = Duration.between(start, Instant.now());
                        log.info("Method {} failed after: {} ms", methodName, duration.toMillis());
                    });
        } else {
            // For non-Uni results, measure time immediately
            Duration duration = Duration.between(start, Instant.now());
            log.info("Method {} execution time: {} ms", methodName, duration.toMillis());
            return result;
        }
    }
}
