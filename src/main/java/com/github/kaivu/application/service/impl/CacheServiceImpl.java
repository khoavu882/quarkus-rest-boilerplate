package com.github.kaivu.application.service.impl;

import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.config.redis.RedisManager;
import com.github.kaivu.config.redis.RedisProfile;
import com.github.kaivu.config.redis.RedisProfileType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
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

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    private final RedisManager redisManager;

    @Inject
    public CacheServiceImpl(@RedisProfile(RedisProfileType.DEFAULT) RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    @Override
    public <T> Uni<Optional<T>> get(String key, Class<T> type) {
        return redisManager
                .get(key, type)
                .invoke(result -> log.debug("Cache {} for key: {}", result.isPresent() ? "hit" : "miss", key));
    }

    @Override
    public <T> Uni<Void> set(String key, T value, Duration ttl) {
        return redisManager
                .set(key, value, ttl)
                .invoke(() -> log.debug("Cached value for key: {} with TTL: {}", key, ttl));
    }

    @Override
    public <T> Uni<Void> set(String key, T value) {
        return set(key, value, DEFAULT_TTL);
    }

    @Override
    public <T> Uni<T> getOrCompute(String key, Class<T> type, Supplier<Uni<T>> supplier, Duration ttl) {
        return redisManager.getOrCompute(key, type, supplier.get(), ttl);
    }

    @Override
    public <T> Uni<T> getOrCompute(String key, Class<T> type, Supplier<Uni<T>> supplier) {
        return getOrCompute(key, type, supplier, DEFAULT_TTL);
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
                .withBackOff(Duration.ofMillis(100))
                .atMost(maxRetries)
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to get value for key: {} after {} retries", key, maxRetries, throwable);
                    return Optional.empty();
                });
    }
}
