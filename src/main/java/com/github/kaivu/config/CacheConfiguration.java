package com.github.kaivu.config;

import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

/**
 * Dedicated cache configuration that follows single responsibility principle.
 * This configuration is focused solely on caching concerns.
 */
@Getter
@ApplicationScoped
public class CacheConfiguration {

    @ConfigMapping(prefix = "app.cache")
    public CacheConfig cache;

    /**
     * Comprehensive cache configuration with TTL settings, prefixes, and operational parameters.
     */
    @Getter
    public static class CacheConfig {
        /**
         * Default TTL in milliseconds for cache entries (1 hour).
         */
        @Positive
        public long defaultTtlMs = 3600000L;

        /**
         * Default expiration duration in milliseconds for cache entries (1 hour).
         */
        @Positive
        public long defaultExpireDurationMs = 3600000L;

        /**
         * Entity device specific cache configuration.
         */
        @NotNull
        public EntityDeviceCacheConfig entityDevice = new EntityDeviceCacheConfig();

        /**
         * Entity device details cache configuration.
         */
        @NotNull
        public EntityDeviceDetailsCacheConfig entityDeviceDetails = new EntityDeviceDetailsCacheConfig();

        /**
         * Entity device page cache configuration.
         */
        @NotNull
        public EntityDevicePageCacheConfig entityDevicePage = new EntityDevicePageCacheConfig();

        /**
         * Media file cache configuration.
         */
        @NotNull
        public MediaFileCacheConfig mediaFile = new MediaFileCacheConfig();

        /**
         * Message interpolator cache configuration.
         */
        @NotNull
        public MessageInterpolatorCacheConfig messageInterpolator = new MessageInterpolatorCacheConfig();

        /**
         * Cache key prefix configuration.
         */
        @NotNull
        public PrefixConfig prefix = new PrefixConfig();
    }

    /**
     * Entity device cache configuration.
     */
    @Getter
    public static class EntityDeviceCacheConfig {
        /**
         * TTL for entity device cache entries (30 minutes).
         */
        @Positive
        public long ttlMs = 1800000L;
    }

    /**
     * Entity device details cache configuration.
     */
    @Getter
    public static class EntityDeviceDetailsCacheConfig {
        /**
         * TTL for entity device details cache entries (1 hour).
         */
        @Positive
        public long ttlMs = 3600000L;
    }

    /**
     * Entity device page cache configuration.
     */
    @Getter
    public static class EntityDevicePageCacheConfig {
        /**
         * TTL for entity device page cache entries (15 minutes).
         */
        @Positive
        public long ttlMs = 900000L;
    }

    /**
     * Media file cache configuration.
     */
    @Getter
    public static class MediaFileCacheConfig {
        /**
         * TTL for media file cache entries (1 hour).
         */
        @Positive
        public long ttlMs = 3600000L;
    }

    /**
     * Message interpolator cache configuration.
     */
    @Getter
    public static class MessageInterpolatorCacheConfig {
        /**
         * TTL for message interpolator cache entries (1 hour).
         */
        @Positive
        public long ttlMs = 3600000L;

        /**
         * Expiration time in hours for cached messages.
         */
        @Positive
        public long expireHours = 1L;

        /**
         * Maximum size of the message interpolator cache.
         */
        @Positive
        public long maxSize = 1000L;
    }

    /**
     * Cache key prefix configuration for different entity types.
     */
    @Getter
    public static class PrefixConfig {
        /**
         * Prefix for entity device cache keys.
         */
        @NotBlank
        public String entityDevice = "entity_device";

        /**
         * Prefix for entity device details cache keys.
         */
        @NotBlank
        public String entityDeviceDetails = "entity_device_details";

        /**
         * Prefix for entity device page cache keys.
         */
        @NotBlank
        public String entityDevicePage = "entity_device_page";

        /**
         * Prefix for media file cache keys.
         */
        @NotBlank
        public String mediaFile = "MediaFile";
    }
}
