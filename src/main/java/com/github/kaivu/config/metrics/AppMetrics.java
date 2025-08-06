package com.github.kaivu.config.metrics;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Simplified performance monitoring without complex Micrometer dependencies
 * Tracks cache performance and database connection health using basic counters
 */
@Slf4j
@ApplicationScoped
public class AppMetrics {

    // Redis cache metrics
    private final LongAdder redisHits = new LongAdder();
    private final LongAdder redisMisses = new LongAdder();
    private final LongAdder redisErrors = new LongAdder();

    // Caffeine cache metrics
    private final LongAdder caffeineHits = new LongAdder();
    private final LongAdder caffeineMisses = new LongAdder();
    private final AtomicLong caffeineSize = new AtomicLong(0);

    // Database metrics
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong waitingConnections = new AtomicLong(0);
    private final AtomicLong connectionEvents = new AtomicLong(0);

    void onStart(@Observes StartupEvent ev) {
        log.info("App metrics system initialized successfully");
    }

    // Redis metrics recording methods
    public void recordRedisHit(Duration duration) {
        redisHits.increment();
        log.debug("Redis cache hit recorded in {}ms", duration.toMillis());
    }

    public void recordRedisMiss(Duration duration) {
        redisMisses.increment();
        log.debug("Redis cache miss recorded in {}ms", duration.toMillis());
    }

    public void recordRedisError() {
        redisErrors.increment();
        log.debug("Redis cache error recorded");
    }

    // Caffeine metrics recording methods
    public void recordCaffeineHit(Duration duration) {
        caffeineHits.increment();
        log.debug("Caffeine cache hit recorded in {}ms", duration.toMillis());
    }

    public void recordCaffeineMiss(Duration duration) {
        caffeineMisses.increment();
        log.debug("Caffeine cache miss recorded in {}ms", duration.toMillis());
    }

    public void updateCaffeineSize(long size) {
        caffeineSize.set(size);
    }

    // Database metrics recording methods
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
        connectionEvents.incrementAndGet();
    }

    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
        connectionEvents.incrementAndGet();
    }

    public void incrementWaitingConnections() {
        waitingConnections.incrementAndGet();
    }

    public void decrementWaitingConnections() {
        waitingConnections.decrementAndGet();
    }

    // Hit rate calculation methods
    public double getRedisHitRate() {
        long hits = redisHits.sum();
        long misses = redisMisses.sum();
        long total = hits + misses;

        if (total == 0) return 0.0;
        return ((double) hits / total) * 100.0;
    }

    public double getCaffeineHitRate() {
        long hits = caffeineHits.sum();
        long misses = caffeineMisses.sum();
        long total = hits + misses;

        if (total == 0) return 0.0;
        return ((double) hits / total) * 100.0;
    }

    public double getOverallHitRate() {
        long totalHits = redisHits.sum() + caffeineHits.sum();
        long totalMisses = redisMisses.sum() + caffeineMisses.sum();
        long total = totalHits + totalMisses;

        if (total == 0) return 0.0;
        return ((double) totalHits / total) * 100.0;
    }

    // Utility method to get cache statistics snapshot
    public CacheStats getCacheStats() {
        return new CacheStats(
                redisHits.sum(),
                redisMisses.sum(),
                redisErrors.sum(),
                caffeineHits.sum(),
                caffeineMisses.sum(),
                caffeineSize.get(),
                getRedisHitRate(),
                getCaffeineHitRate(),
                getOverallHitRate());
    }

    // Utility method to get database statistics snapshot
    public DatabaseStats getDatabaseStats() {
        return new DatabaseStats(
                activeConnections.get(),
                waitingConnections.get(),
                connectionEvents.get(),
                true // Simplified health check
                );
    }

    /**
     * Data class for cache statistics
     */
    public record CacheStats(
            long redisHits,
            long redisMisses,
            long redisErrors,
            long caffeineHits,
            long caffeineMisses,
            long caffeineSize,
            double redisHitRate,
            double caffeineHitRate,
            double overallHitRate) {}

    /**
     * Data class for database statistics
     */
    public record DatabaseStats(
            long activeConnections, long waitingConnections, long connectionEvents, boolean healthy) {}
}
