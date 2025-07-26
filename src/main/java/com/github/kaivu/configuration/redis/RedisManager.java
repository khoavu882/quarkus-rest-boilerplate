package com.github.kaivu.configuration.redis;

import com.github.kaivu.adapter.out.client.RedisHelper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified Redis manager using Helper pattern only
 * Provides easy access to Redis operations without complexity
 */
@Slf4j
@ApplicationScoped
public class RedisManager {

    private final Map<RedisProfileType, RedisHelper> redisHelpers = new ConcurrentHashMap<>();
    private static final String KEY_SEPARATOR = ":";

    @Inject
    @RedisProfile(RedisProfileType.DEFAULT)
    RedisHelper defaultRedisHelper;

    @Inject
    @RedisProfile(RedisProfileType.DEMO)
    RedisHelper demoRedisHelper;

    /**
     * Get Redis helper for specific profile
     */
    public RedisHelper getHelper(RedisProfileType profile) {
        return redisHelpers.computeIfAbsent(profile, this::resolveHelper);
    }

    /**
     * Get default Redis helper (most common use case)
     */
    public RedisHelper getHelper() {
        return getHelper(RedisProfileType.DEFAULT);
    }

    // Convenience methods for common operations
    public <T> Uni<Optional<T>> get(String key, Class<T> type) {
        return getHelper().get(key, type);
    }

    public <T> Uni<Optional<T>> get(RedisProfileType profile, String key, Class<T> type) {
        return getHelper(profile).get(key, type);
    }

    public <T> Uni<Void> set(String key, T value, Duration ttl) {
        return getHelper().set(key, value, ttl);
    }

    public <T> Uni<Void> set(RedisProfileType profile, String key, T value, Duration ttl) {
        return getHelper(profile).set(key, value, ttl);
    }

    public <T> Uni<T> getOrCompute(String key, Class<T> type, Uni<T> loader, Duration ttl) {
        return getHelper().getOrCompute(key, type, loader, ttl);
    }

    public <T> Uni<T> getOrCompute(RedisProfileType profile, String key, Class<T> type, Uni<T> loader, Duration ttl) {
        return getHelper(profile).getOrCompute(key, type, loader, ttl);
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
        return getHelper().delete(key);
    }

    public Uni<Boolean> exists(String key) {
        return getHelper().exists(key);
    }

    public <T> Uni<Void> setMultiple(Map<String, T> keyValueMap, Duration ttl) {
        return getHelper().setMultiple(keyValueMap, ttl);
    }

    private RedisHelper resolveHelper(RedisProfileType profile) {
        return switch (profile) {
            case DEFAULT -> defaultRedisHelper;
            case DEMO -> demoRedisHelper;
            case CACHE, SESSION -> defaultRedisHelper; // Fallback to default
        };
    }
}
