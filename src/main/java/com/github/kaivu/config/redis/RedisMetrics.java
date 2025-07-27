package com.github.kaivu.config.redis;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis metrics and monitoring configuration
 * Provides observability for Redis operations following project standards
 */
@Slf4j
@ApplicationScoped
public class RedisMetrics {

    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Counter cacheErrors;
    private final Timer cacheOperationTimer;
    private final AtomicLong activeCacheConnections;

    @Inject
    public RedisMetrics(MeterRegistry meterRegistry) {
        this.cacheHits = Counter.builder("redis.cache.hits")
                .description("Number of cache hits")
                .register(meterRegistry);

        this.cacheMisses = Counter.builder("redis.cache.misses")
                .description("Number of cache misses")
                .register(meterRegistry);

        this.cacheErrors = Counter.builder("redis.cache.errors")
                .description("Number of cache errors")
                .register(meterRegistry);

        this.cacheOperationTimer = Timer.builder("redis.cache.operation.duration")
                .description("Cache operation duration")
                .register(meterRegistry);

        this.activeCacheConnections = meterRegistry.gauge("redis.connections.active", new AtomicLong(0));
    }

    public void recordCacheHit() {
        cacheHits.increment();
    }

    public void recordCacheMiss() {
        cacheMisses.increment();
    }

    public void recordCacheError() {
        cacheErrors.increment();
    }

    public <T> Uni<T> timeOperation(Uni<T> operation) {
        return operation
                .invoke(() -> cacheOperationTimer.record(Duration.ofMillis(System.currentTimeMillis())))
                .onFailure()
                .invoke(throwable -> recordCacheError());
    }

    public void incrementActiveConnections() {
        activeCacheConnections.incrementAndGet();
    }

    public void decrementActiveConnections() {
        activeCacheConnections.decrementAndGet();
    }
}
