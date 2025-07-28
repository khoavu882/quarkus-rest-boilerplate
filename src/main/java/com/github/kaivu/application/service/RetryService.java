package com.github.kaivu.application.service;

import com.github.kaivu.application.exception.EntityNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@ApplicationScoped
public class RetryService {

    public <T> Uni<T> withRetry(Uni<T> operation, String operationName) {
        return operation
                .onFailure(this::isRetryable)
                .retry()
                .withBackOff(Duration.ofMillis(100), Duration.ofSeconds(2))
                .atMost(3)
                .onFailure()
                .invoke(ex -> log.error("Operation {} failed after retries: {}", operationName, ex.getMessage()));
    }

    private boolean isRetryable(Throwable throwable) {
        return !(throwable instanceof EntityNotFoundException || throwable instanceof ConstraintViolationException);
    }
}
