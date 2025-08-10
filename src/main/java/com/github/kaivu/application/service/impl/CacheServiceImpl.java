package com.github.kaivu.application.service.impl;

import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.config.ApplicationConfiguration;
import com.github.kaivu.config.metrics.AppMetrics;
import com.github.kaivu.config.redis.RedisManager;
import com.github.kaivu.config.redis.RedisProfile;
import com.github.kaivu.config.redis.RedisProfileType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Simplified Cache Service using Helper pattern only
 * Follows hexagonal architecture and project standards
 * Uses DEFAULT profile Redis connection for cache operations
 */
@Slf4j
@ApplicationScoped
public class CacheServiceImpl implements CacheService {

    private final ApplicationConfiguration config;
    private final RedisManager redisManager;
    private final AppMetrics simpleMetrics;

    @Inject
    public CacheServiceImpl(
            ApplicationConfiguration config,
            @RedisProfile(RedisProfileType.DEFAULT) RedisManager redisManager,
            AppMetrics simpleMetrics) {
        this.config = config;
        this.redisManager = redisManager;
        this.simpleMetrics = simpleMetrics;
    }

    @Override
    public <T> Uni<Optional<T>> get(String key, Class<T> type) {
        Instant start = Instant.now();
        return redisManager
                .get(key, type)
                .invoke(result -> {
                    Duration duration = Duration.between(start, Instant.now());
                    if (result.isPresent()) {
                        simpleMetrics.recordRedisHit(duration);
                        log.debug("Cache hit for key: {}", key);
                    } else {
                        simpleMetrics.recordRedisMiss(duration);
                        log.debug("Cache miss for key: {}", key);
                    }
                })
                .onFailure()
                .invoke(throwable -> {
                    simpleMetrics.recordRedisError();
                    log.error("Cache error for key: {}", key, throwable);
                });
    }

    @Override
    public <T> Uni<Void> set(String key, T value, Duration ttl) {
        Instant start = Instant.now();
        return redisManager
                .set(key, value, ttl)
                .invoke(() -> {
                    Duration duration = Duration.between(start, Instant.now());
                    // Recording set operation (no method in SimpleMetrics yet)
                    log.debug("Redis SET operation took {}ms", duration.toMillis());
                    log.debug("Cached value for key: {} with TTL: {}", key, ttl);
                })
                .onFailure()
                .invoke(throwable -> {
                    simpleMetrics.recordRedisError();
                    log.error("Failed to cache value for key: {}", key, throwable);
                });
    }

    @Override
    public <T> Uni<Void> set(String key, T value) {
        return set(key, value, Duration.ofMillis(config.cache.defaultExpireDurationMs));
    }

    @Override
    public <T> Uni<T> getOrCompute(String key, Class<T> type, Supplier<Uni<T>> supplier, Duration ttl) {
        return redisManager.getOrCompute(key, type, supplier.get(), ttl);
    }

    @Override
    public <T> Uni<T> getOrCompute(String key, Class<T> type, Supplier<Uni<T>> supplier) {
        return getOrCompute(key, type, supplier, Duration.ofMillis(config.cache.defaultExpireDurationMs));
    }

    @Override
    public Uni<Boolean> delete(String key) {
        return redisManager.delete(key).invoke(deleted -> log.debug("Delete key: {} - Success: {}", key, deleted));
    }

    @Override
    public Uni<Long> deleteByPattern(String pattern) {
        return redisManager
                .deleteByPattern(pattern)
                .invoke(count -> log.debug("Deleted {} keys matching pattern: {}", count, pattern))
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to delete keys by pattern: {}", pattern, throwable);
                    return 0L;
                });
    }

    @Override
    public Uni<Boolean> exists(String key) {
        return redisManager.exists(key);
    }

    @Override
    public <T> Uni<Void> setMultiple(Map<String, T> keyValueMap, Duration ttl) {
        return redisManager
                .setMultiple(keyValueMap, ttl)
                .invoke(() -> log.debug("Set {} keys with TTL: {}", keyValueMap.size(), ttl));
    }

    @Override
    public Uni<Long> increment(String key, long delta) {
        return redisManager
                .increment(key, delta)
                .invoke(newValue -> log.debug("Incremented key: {} by {} to {}", key, delta, newValue))
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to increment key: {} by {}", key, delta, throwable);
                    return delta;
                });
    }

    @Override
    public Uni<Long> increment(String key) {
        return increment(key, 1L);
    }

    @Override
    public String generateKey(String prefix, String... identifiers) {
        return redisManager.generateKey(prefix, identifiers);
    }

    @Override
    public <T> Uni<Void> warmUp(String key, Class<T> type, Supplier<Uni<T>> dataLoader, Duration ttl) {
        return exists(key)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.debug("Cache already warm for key: {}", key);
                        return Uni.createFrom().voidItem();
                    } else {
                        log.debug("Warming up cache for key: {}", key);
                        return dataLoader
                                .get()
                                .flatMap(data -> set(key, data, ttl))
                                .invoke(() -> log.debug("Cache warmed up for key: {}", key));
                    }
                })
                .onFailure()
                .invoke(throwable -> log.error("Failed to warm up cache for key: {}", key, throwable));
    }

    @Override
    public <T> Uni<Optional<T>> getWithRetry(String key, Class<T> type, int maxRetries) {
        return get(key, type)
                .onFailure()
                .retry()
                .withBackOff(java.time.Duration.ofMillis(config.retry.cache.backoffMs))
                .atMost(maxRetries)
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to get value for key: {} after {} retries", key, maxRetries, throwable);
                    return Optional.empty();
                });
    }
}
