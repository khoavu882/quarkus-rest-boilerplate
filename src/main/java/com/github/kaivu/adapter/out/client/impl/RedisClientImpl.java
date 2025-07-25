package com.github.kaivu.adapter.out.client.impl;

import com.github.kaivu.adapter.out.client.RedisClient;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/19/25
 * Time: 5:18 PM
 */
@Slf4j
@ApplicationScoped
public class RedisClientImpl implements RedisClient {

    private final ReactiveRedisDataSource reactiveDataSource;

    public RedisClientImpl(ReactiveRedisDataSource reactiveDataSource) {
        this.reactiveDataSource = reactiveDataSource;
    }

    /**
     * Get cached value reactively
     *
     * @param key Cache key
     * @return Uni with cached value or null if not found
     */
    public <T> Uni<T> get(String key, Class<T> type) {
        ReactiveValueCommands<String, T> commands = reactiveDataSource.value(type);
        return commands.get(key);
    }

    /**
     * Set value in cache reactively
     *
     * @param key Cache key
     * @param value Value to cache
     * @param ttlSeconds Time to live in seconds
     * @return Uni<Void> representing the completion of the operation
     */
    public <T> Uni<Void> set(String key, T value, long ttlSeconds) {
        ReactiveValueCommands<String, T> commands = reactiveDataSource.value((Class<T>) value.getClass());
        return commands.setex(key, ttlSeconds, value)
                .invoke(() -> log.debug("Cached value for key: {} with TTL: {} seconds", key, ttlSeconds))
                .replaceWithVoid();
    }

    /**
     * Delete a key from cache reactively
     *
     * @param key Cache key to delete
     * @return Uni<Void> representing the completion of the operation
     */
    public Uni<Void> delete(String key) {
        ReactiveValueCommands<String, String> commands = reactiveDataSource.value(String.class);
        return commands.getdel(key)
                .invoke(() -> log.debug("Deleted cache for key: {}", key))
                .replaceWithVoid();
    }

    /**
     * Check if a key exists in cache reactively
     *
     * @param key Cache key to check
     * @return Uni<Boolean> with true if key exists, false otherwise
     */
    public Uni<Boolean> exists(String key) {
        ReactiveValueCommands<String, String> commands = reactiveDataSource.value(String.class);
        return commands.get(key).map(Objects::nonNull);
    }
}
