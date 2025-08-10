package com.github.kaivu.application.service;

import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.config.AppConfiguration;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class RetryService {

    private final AppConfiguration config;

    @Inject
    public RetryService(AppConfiguration config) {
        this.config = config;
    }

    public <T> Uni<T> withRetry(Uni<T> operation, String operationName) {
        return operation
                .onFailure(this::isRetryable)
                .retry()
                .withBackOff(
                        java.time.Duration.ofMillis(
                                config.retry().defaultRetry().backoffMinMs()),
                        java.time.Duration.ofMillis(
                                config.retry().defaultRetry().backoffMaxMs()))
                .atMost(config.retry().defaultRetry().maxAttempts())
                .onFailure()
                .invoke(ex -> log.error("Operation {} failed after retries: {}", operationName, ex.getMessage()));
    }

    private boolean isRetryable(Throwable throwable) {
        return !(throwable instanceof EntityNotFoundException || throwable instanceof ConstraintViolationException);
    }
}
