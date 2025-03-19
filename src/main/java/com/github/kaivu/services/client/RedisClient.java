package com.github.kaivu.services.client;

import io.smallrye.mutiny.Uni;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/19/25
 * Time: 5:18 PM
 */
public interface RedisClient {

    /**
     * Get cached value reactively
     *
     * @param key Cache key
     * @return Uni with cached value or null if not found
     */
    <T> Uni<T> get(String key, Class<T> type);

    /**
     * Set value in cache reactively
     *
     * @param key Cache key
     * @param value Value to cache
     * @param ttlSeconds Time to live in seconds
     * @return Uni<Void> representing the completion of the operation
     */
    <T> Uni<Void> set(String key, T value, long ttlSeconds);

    /**
     * Delete a key from cache reactively
     *
     * @param key Cache key to delete
     * @return Uni<Void> representing the completion of the operation
     */
    Uni<Void> delete(String key);

    /**
     * Check if a key exists in cache reactively
     *
     * @param key Cache key to check
     * @return Uni<Boolean> with true if key exists, false otherwise
     */
    Uni<Boolean> exists(String key);
}
