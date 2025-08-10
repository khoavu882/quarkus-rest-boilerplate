package com.github.kaivu.adapter.out.client.impl;

import com.github.kaivu.adapter.out.client.RedisHelper;
import com.github.kaivu.config.AppConfiguration;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Redis Helper implementation providing high-level cache operations
 * Follows hexagonal architecture pattern for adapter layer
 */
@Slf4j
@ApplicationScoped
public class RedisHelperImpl implements RedisHelper {

    private final ReactiveRedisDataSource reactiveDataSource;
    private final AppConfiguration config;

    @Inject
    public RedisHelperImpl(ReactiveRedisDataSource reactiveDataSource, AppConfiguration config) {
        this.reactiveDataSource = reactiveDataSource;
        this.config = config;
    }

    @Override
    public <T> Uni<Optional<T>> get(String key, Class<T> type) {
        ReactiveValueCommands<String, T> commands = reactiveDataSource.value(type);
        return commands.get(key).map(Optional::ofNullable).onFailure().recoverWithItem(throwable -> {
            log.warn("Failed to get value for key: {}", key, throwable);
            return Optional.empty();
        });
    }

    @Override
    public <T> Uni<T> getOrCompute(String key, Class<T> type, Uni<T> fallback, Duration ttl) {
        return get(key, type).flatMap(optional -> {
            if (optional.isPresent()) {
                log.debug("Cache hit for key: {}", key);
                return Uni.createFrom().item(optional.get());
            } else {
                log.debug("Cache miss for key: {}, computing fallback", key);
                return fallback.flatMap(value -> set(key, value, ttl).map(ignored -> value));
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Uni<Void> set(String key, T value, Duration ttl) {
        ReactiveValueCommands<String, T> commands = reactiveDataSource.value((Class<T>) value.getClass());
        return commands.setex(key, ttl.getSeconds(), value)
                .invoke(() -> log.debug("Cached value for key: {} with TTL: {}", key, ttl))
                .replaceWithVoid()
                .onFailure()
                .invoke(throwable -> log.error("Failed to set value for key: {}", key, throwable));
    }

    @Override
    public <T> Uni<Void> set(String key, T value) {
        return set(key, value, Duration.ofMillis(config.cache().defaultExpireDurationMs()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Uni<Void> setMultiple(Map<String, T> keyValueMap, Duration ttl) {
        if (keyValueMap.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        // Get the first value to determine the type
        T firstValue = keyValueMap.values().iterator().next();
        ReactiveValueCommands<String, T> commands = reactiveDataSource.value((Class<T>) firstValue.getClass());

        return Uni.join()
                .all(keyValueMap.entrySet().stream()
                        .map(entry -> commands.setex(entry.getKey(), ttl.getSeconds(), entry.getValue()))
                        .toList())
                .andFailFast()
                .invoke(() -> log.debug("Set {} keys with TTL: {}", keyValueMap.size(), ttl))
                .replaceWithVoid()
                .onFailure()
                .invoke(throwable -> log.error("Failed to set multiple values", throwable));
    }

    @Override
    public Uni<Boolean> delete(String key) {
        ReactiveKeyCommands<String> keyCommands = reactiveDataSource.key();
        return keyCommands
                .del(key)
                .map(deletedCount -> deletedCount > 0)
                .invoke(deleted -> log.debug("Delete key: {} - Success: {}", key, deleted))
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to delete key: {}", key, throwable);
                    return false;
                });
    }

    @Override
    public Uni<Long> delete(List<String> keys) {
        if (keys.isEmpty()) {
            return Uni.createFrom().item(0L);
        }

        ReactiveKeyCommands<String> keyCommands = reactiveDataSource.key();
        return keyCommands
                .del(keys.toArray(new String[0]))
                .map(Integer::longValue) // Convert Integer to Long
                .invoke(deletedCount -> log.debug("Deleted {} out of {} keys", deletedCount, keys.size()))
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to delete keys: {}", keys, throwable);
                    return 0L;
                });
    }

    @Override
    public Uni<Long> deleteByPattern(String pattern) {
        ReactiveKeyCommands<String> keyCommands = reactiveDataSource.key();
        return keyCommands
                .keys(pattern)
                .flatMap(keys -> {
                    if (keys.isEmpty()) {
                        log.debug("No keys found matching pattern: {}", pattern);
                        return Uni.createFrom().item(0L);
                    }
                    return keyCommands
                            .del(keys.toArray(new String[0]))
                            .map(Integer::longValue)
                            .invoke(deletedCount ->
                                    log.debug("Deleted {} keys matching pattern: {}", deletedCount, pattern));
                })
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to delete keys by pattern: {}", pattern, throwable);
                    return 0L;
                });
    }

    @Override
    public Uni<Boolean> exists(String key) {
        // Use get() to check if key exists - simpler and avoids type issues
        return reactiveDataSource
                .value(String.class)
                .get(key)
                .map(Objects::nonNull)
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to check existence of key: {}", key, throwable);
                    return false;
                });
    }

    @Override
    public Uni<Long> getTtl(String key) {
        ReactiveKeyCommands<String> keyCommands = reactiveDataSource.key();
        return keyCommands.ttl(key).onFailure().recoverWithItem(throwable -> {
            log.error("Failed to get TTL for key: {}", key, throwable);
            return -2L; // Key doesn't exist
        });
    }

    @Override
    public Uni<Long> increment(String key, long delta) {
        ReactiveValueCommands<String, Long> commands = reactiveDataSource.value(Long.class);
        return commands.incrby(key, delta)
                .invoke(newValue -> log.debug("Incremented key: {} by {} to {}", key, delta, newValue))
                .onFailure()
                .recoverWithItem(throwable -> {
                    log.error("Failed to increment key: {} by {}", key, delta, throwable);
                    return 0L;
                });
    }

    @Override
    public Uni<Long> increment(String key) {
        return increment(key, 1L);
    }

    @Override
    public Uni<Void> clear() {
        // Note: Redis FLUSHALL is not available in reactive commands
        // This is a simplified implementation that removes keys by pattern
        ReactiveKeyCommands<String> keyCommands = reactiveDataSource.key();
        return keyCommands
                .keys("*")
                .flatMap(keys -> {
                    if (keys.isEmpty()) {
                        return Uni.createFrom().voidItem();
                    }
                    return keyCommands.del(keys.toArray(new String[0])).replaceWithVoid();
                })
                .invoke(() -> log.warn("Attempted to clear cache entries"))
                .onFailure()
                .invoke(throwable -> log.error("Failed to clear cache: {}", throwable.getMessage()));
    }
}
