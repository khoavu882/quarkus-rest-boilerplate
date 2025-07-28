package com.github.kaivu.config.redis;

import com.github.kaivu.adapter.out.client.RedisHelper;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Simplified Redis manager using Helper pattern only
 * This is NOT a CDI bean - instances are created by RedisManagerProvider
 * Consumers inject this with @RedisProfile annotation to specify the profile they want
 */
@Slf4j
public class RedisManager {

    private final RedisHelper redisHelper;
    private static final String KEY_SEPARATOR = ":";

    public RedisManager(RedisHelper redisHelper) {
        this.redisHelper = redisHelper;
    }

    // Core Redis operations - profile is determined by the injected helper
    public <T> Uni<Optional<T>> get(String key, Class<T> type) {
        return redisHelper.get(key, type);
    }

    public <T> Uni<Void> set(String key, T value, Duration ttl) {
        return redisHelper.set(key, value, ttl);
    }

    public <T> Uni<T> getOrCompute(String key, Class<T> type, Uni<T> loader, Duration ttl) {
        return redisHelper.getOrCompute(key, type, loader, ttl);
    }

    public String generateKey(String prefix, String... identifiers) {
        if (identifiers == null || identifiers.length == 0) {
            return prefix;
        }

        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (String identifier : identifiers) {
            if (identifier != null && !identifier.trim().isEmpty()) {
                keyBuilder.append(KEY_SEPARATOR).append(identifier.trim());
            }
        }
        return keyBuilder.toString();
    }

    public Uni<Boolean> delete(String key) {
        return redisHelper.delete(key);
    }

    public Uni<Long> deleteByPattern(String pattern) {
        return redisHelper.deleteByPattern(pattern);
    }

    public Uni<Boolean> exists(String key) {
        return redisHelper.exists(key);
    }

    public <T> Uni<Void> setMultiple(Map<String, T> keyValueMap, Duration ttl) {
        return redisHelper.setMultiple(keyValueMap, ttl);
    }

    public Uni<Long> increment(String key, long delta) {
        return redisHelper.increment(key, delta);
    }

    public Uni<Long> increment(String key) {
        return increment(key, 1L);
    }
}
