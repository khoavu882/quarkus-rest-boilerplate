package com.github.kaivu.application.service;

import io.smallrye.mutiny.Uni;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Enhanced Redis Cache Service providing simplified caching operations
 * Following the project's service architecture pattern
 */
public interface CacheService {

    /**
     * Get value from cache with automatic type handling
     */
    <T> Uni<Optional<T>> get(String key, Class<T> type);

    /**
     * Set value in cache with TTL
     */
    <T> Uni<Void> set(String key, T value, Duration ttl);

    /**
     * Set value in cache with default TTL (1 hour)
     */
    <T> Uni<Void> set(String key, T value);

    /**
     * Cache-aside pattern: get from cache or compute and cache
     */
    <T> Uni<T> getOrCompute(String key, Class<T> type, Supplier<Uni<T>> supplier, Duration ttl);

    /**
     * Cache-aside pattern with default TTL
     */
    <T> Uni<T> getOrCompute(String key, Class<T> type, Supplier<Uni<T>> supplier);

    /**
     * Delete single key
     */
    Uni<Boolean> delete(String key);

    /**
     * Delete multiple keys by pattern
     */
    Uni<Long> deleteByPattern(String pattern);

    /**
     * Check if key exists
     */
    Uni<Boolean> exists(String key);

    /**
     * Set multiple key-value pairs atomically
     */
    <T> Uni<Void> setMultiple(Map<String, T> keyValueMap, Duration ttl);

    /**
     * Increment numeric value
     */
    Uni<Long> increment(String key, long delta);

    /**
     * Increment by 1
     */
    Uni<Long> increment(String key);

    /**
     * Generate standardized cache key
     */
    String generateKey(String prefix, String... identifiers);

    /**
     * Warm up cache with initial data
     */
    <T> Uni<Void> warmUp(String key, Class<T> type, Supplier<Uni<T>> dataLoader, Duration ttl);

    /**
     * Get with automatic retry on failure
     */
    <T> Uni<Optional<T>> getWithRetry(String key, Class<T> type, int maxRetries);
}
