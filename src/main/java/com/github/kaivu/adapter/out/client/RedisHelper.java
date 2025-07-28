package com.github.kaivu.adapter.out.client;

import io.smallrye.mutiny.Uni;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Redis Helper providing high-level cache operations with enhanced functionality
 *
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/19/25
 * Time: 5:18 PM
 */
public interface RedisHelper {

    /**
     * Get cached value with optional fallback
     *
     * @param key Cache key
     * @param type Value type
     * @return Uni with Optional containing cached value
     */
    <T> Uni<Optional<T>> get(String key, Class<T> type);

    /**
     * Get cached value with fallback supplier
     *
     * @param key Cache key
     * @param type Value type
     * @param fallback Supplier to provide value if not cached
     * @param ttl Time to live for cached value
     * @return Uni with cached or fallback value
     */
    <T> Uni<T> getOrCompute(String key, Class<T> type, Uni<T> fallback, Duration ttl);

    /**
     * Set value in cache with Duration TTL
     *
     * @param key Cache key
     * @param value Value to cache
     * @param ttl Time to live duration
     * @return Uni representing completion
     */
    <T> Uni<Void> set(String key, T value, Duration ttl);

    /**
     * Set value in cache with default TTL
     *
     * @param key Cache key
     * @param value Value to cache
     * @return Uni representing completion
     */
    <T> Uni<Void> set(String key, T value);

    /**
     * Set multiple key-value pairs
     *
     * @param keyValueMap Map of keys and values
     * @param ttl Time to live for all entries
     * @return Uni representing completion
     */
    <T> Uni<Void> setMultiple(Map<String, T> keyValueMap, Duration ttl);

    /**
     * Delete a key from cache
     *
     * @param key Cache key to delete
     * @return Uni with true if key was deleted, false if key didn't exist
     */
    Uni<Boolean> delete(String key);

    /**
     * Delete multiple keys from cache
     *
     * @param keys Cache keys to delete
     * @return Uni with number of keys deleted
     */
    Uni<Long> delete(List<String> keys);

    /**
     * Delete keys matching a pattern
     *
     * @param pattern Pattern to match keys (e.g., "prefix:*")
     * @return Uni with number of keys deleted
     */
    Uni<Long> deleteByPattern(String pattern);

    /**
     * Check if a key exists in cache
     *
     * @param key Cache key to check
     * @return Uni with true if key exists, false otherwise
     */
    Uni<Boolean> exists(String key);

    /**
     * Get remaining TTL for a key
     *
     * @param key Cache key
     * @return Uni with remaining TTL in seconds, -1 if key has no expiry, -2 if key doesn't exist
     */
    Uni<Long> getTtl(String key);

    /**
     * Increment a numeric value in cache
     *
     * @param key Cache key
     * @param delta Amount to increment by
     * @return Uni with new value after increment
     */
    Uni<Long> increment(String key, long delta);

    /**
     * Increment a numeric value in cache by 1
     *
     * @param key Cache key
     * @return Uni with new value after increment
     */
    Uni<Long> increment(String key);

    /**
     * Clear all cache entries (use with caution)
     *
     * @return Uni representing completion
     */
    Uni<Void> clear();
}
