package com.github.kaivu.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

/**
 * Centralized application configuration that consolidates all configuration properties
 * for the Quarkus REST application. This configuration follows hexagonal architecture
 * principles and provides proper dependency injection support with type-safe nested configurations.
 *
 * <p>All configuration properties are organized into logical groupings using @ConfigMapping
 * for better type safety, validation, and maintainability.
 *
 * @since 1.0.0
 */
@Getter
@ApplicationScoped
public class ApplicationConfiguration {

    // ==================== CONFIGURATION MAPPINGS ====================

    /**
     * Comprehensive cache configuration including TTL settings, prefixes, and size limits.
     */
    @ConfigMapping(prefix = "app.cache")
    public CacheConfig cache;

    /**
     * Retry configuration for various operations including backoff strategies.
     */
    @ConfigMapping(prefix = "app.retry")
    public RetryConfig retry;

    /**
     * Database-related configuration including constraints and audit settings.
     */
    @ConfigMapping(prefix = "app.database")
    public DatabaseConfig database;

    /**
     * Observability configuration for metrics, tracing, and health checks.
     */
    @ConfigMapping(prefix = "app.observability")
    public ObservabilityConfig observability;

    /**
     * Timeout configuration for various operations.
     */
    @ConfigMapping(prefix = "app.timeout")
    public TimeoutConfig timeout;

    /**
     * Health check configuration and thresholds.
     */
    @ConfigMapping(prefix = "app.health")
    public HealthConfig health;

    /**
     * Pagination configuration for REST API responses.
     */
    @ConfigMapping(prefix = "app.pagination")
    public PaginationConfig pagination;

    /**
     * Internationalization configuration for message bundles.
     */
    @ConfigMapping(prefix = "app.i18n")
    public I18nConfig i18n;

    /**
     * Custom HTTP configuration for application-specific HTTP settings.
     */
    @ConfigMapping(prefix = "app.http")
    public HttpConfig http;

    /**
     * MinIO object storage configuration with multiple profiles.
     */
    @ConfigMapping(prefix = "minio")
    public MinioConfig minio;

    /**
     * Quarkus application configuration.
     */
    @ConfigMapping(prefix = "quarkus.application")
    public QuarkusApplicationConfig quarkusApplication;

    /**
     * Quarkus HTTP configuration.
     */
    @ConfigMapping(prefix = "quarkus.http")
    public QuarkusHttpConfig quarkusHttp;

    /**
     * Quarkus Hibernate ORM configuration.
     */
    @ConfigMapping(prefix = "quarkus.hibernate-orm")
    public QuarkusHibernateConfig quarkusHibernate;

    // ==================== CONFIGURATION CLASSES ====================

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

    /**
     * Retry configuration for various operations with backoff strategies.
     */
    @Getter
    public static class RetryConfig {
        /**
         * Maximum number of retry attempts.
         */
        @Min(1)
        @Max(10)
        public int maxAttempts = 3;

        /**
         * Initial backoff delay in milliseconds.
         */
        @Positive
        public long initialBackoffMs = 100L;

        /**
         * Maximum backoff delay in milliseconds.
         */
        @Positive
        public long maxBackoffMs = 2000L;

        /**
         * Cache-specific retry settings.
         */
        @NotNull
        public CacheRetryConfig cache = new CacheRetryConfig();

        /**
         * Default retry settings for backward compatibility.
         */
        @NotNull
        public DefaultRetryConfig defaultRetry = new DefaultRetryConfig();
    }

    /**
     * Cache-specific retry configuration.
     */
    @Getter
    public static class CacheRetryConfig {
        /**
         * Maximum retry attempts for cache operations.
         */
        @Min(1)
        @Max(10)
        public int maxAttempts = 3;

        /**
         * Backoff delay for cache retry operations in milliseconds.
         */
        @Positive
        public long backoffMs = 100L;
    }

    /**
     * Default retry configuration for general operations.
     */
    @Getter
    public static class DefaultRetryConfig {
        /**
         * Maximum retry attempts for default operations.
         */
        @Min(1)
        @Max(10)
        public int maxAttempts = 3;

        /**
         * Minimum backoff delay in milliseconds.
         */
        @Positive
        public long backoffMinMs = 100L;

        /**
         * Maximum backoff delay in milliseconds.
         */
        @Positive
        public long backoffMaxMs = 2000L;
    }

    /**
     * Database-related configuration including entity constraints and audit settings.
     */
    @Getter
    public static class DatabaseConfig {
        /**
         * Entity-specific database configuration.
         */
        @NotNull
        public EntityConfig entity = new EntityConfig();

        /**
         * Audit-specific database configuration.
         */
        @NotNull
        public AuditConfig audit = new AuditConfig();
    }

    /**
     * Entity-specific database configuration.
     */
    @Getter
    public static class EntityConfig {
        /**
         * Maximum length for entity names.
         */
        @Min(50)
        @Max(1000)
        public int nameMaxLength = 500;

        /**
         * Maximum length for entity descriptions.
         */
        @Min(100)
        @Max(5000)
        public int descriptionMaxLength = 2000;
    }

    /**
     * Audit-specific database configuration.
     */
    @Getter
    public static class AuditConfig {
        /**
         * Maximum length for audit user field.
         */
        @Min(10)
        @Max(100)
        public int userFieldLength = 50;

        /**
         * Default user for audit operations.
         */
        @NotBlank
        public String defaultUser = "anonymous";
    }

    /**
     * Observability configuration for metrics, tracing, and monitoring.
     */
    @Getter
    public static class ObservabilityConfig {
        /**
         * Metrics-specific configuration.
         */
        @NotNull
        public MetricsConfig metrics = new MetricsConfig();

        /**
         * Health check ID configuration.
         */
        @NotNull
        public HealthCheckIdConfig healthCheckId = new HealthCheckIdConfig();
    }

    /**
     * Metrics configuration for observability.
     */
    @Getter
    public static class MetricsConfig {
        /**
         * Histogram percentiles for metrics collection.
         */
        @NotBlank
        public String histogramPercentiles = "0.5,0.95,0.99,0.999";
    }

    /**
     * Health check ID configuration.
     */
    @Getter
    public static class HealthCheckIdConfig {
        /**
         * Timestamp modulo for health check IDs.
         */
        @Positive
        public long timestampMod = 1000000L;
    }

    /**
     * Timeout configuration for various operations.
     */
    @Getter
    public static class TimeoutConfig {
        /**
         * Health check timeout in milliseconds.
         */
        @Positive
        public long healthCheckMs = 10000L;

        /**
         * Cache operation timeout in milliseconds.
         */
        @Positive
        public long cacheOperationMs = 5000L;
    }

    /**
     * Health check configuration and thresholds.
     */
    @Getter
    public static class HealthConfig {
        /**
         * Error rate threshold for health checks (percentage).
         */
        @Min(0)
        @Max(100)
        public double errorRateThreshold = 5.0;

        /**
         * Response time threshold in milliseconds.
         */
        @Positive
        public long responseTimeThreshold = 1000L;

        /**
         * Cache test timeout in milliseconds.
         */
        @Positive
        public long cacheTestTimeoutMs = 5000L;

        /**
         * Cache test TTL in milliseconds.
         */
        @Positive
        public long cacheTestTtlMs = 60000L;
    }

    /**
     * Pagination configuration for REST API responses.
     */
    @Getter
    public static class PaginationConfig {
        /**
         * Header configuration for pagination.
         */
        @NotNull
        public HeaderConfig header = new HeaderConfig();
    }

    /**
     * Header configuration for pagination responses.
     */
    @Getter
    public static class HeaderConfig {
        /**
         * Header name for total count in pagination responses.
         */
        @NotBlank
        public String totalCount = "X-Total-Count";

        /**
         * Link format template for pagination headers.
         */
        @NotBlank
        public String linkFormat = "<{0}>; rel=\"{1}\"";
    }

    /**
     * Internationalization configuration for message bundles and localization.
     */
    @Getter
    public static class I18nConfig {
        /**
         * Base path for i18n resource bundles.
         */
        @NotBlank
        public String path = "i18n";

        /**
         * Path to error message bundles.
         */
        @NotBlank
        public String errorMessages = "i18n/error_messages";

        /**
         * Path to validation message bundles.
         */
        @NotBlank
        public String validationMessages = "i18n/validation_messages";
    }

    /**
     * Custom HTTP configuration for application-specific HTTP settings.
     */
    @Getter
    public static class HttpConfig {
        /**
         * Enable HTTP compression.
         */
        public boolean enableCompression = true;

        /**
         * Enable authentication logging.
         */
        public boolean authLogging = true;
    }

    /**
     * MinIO object storage configuration with multiple profile support.
     */
    @Getter
    public static class MinioConfig {
        /**
         * MinIO server URL.
         */
        @NotBlank
        public String url;

        /**
         * MinIO access key.
         */
        @NotBlank
        public String accessKey;

        /**
         * MinIO secret key.
         */
        @NotBlank
        public String secretKey;

        /**
         * Web profile configuration for MinIO.
         */
        @NotNull
        public MinioProfileConfig web = new MinioProfileConfig();

        /**
         * Media profile configuration for MinIO.
         */
        @NotNull
        public MinioProfileConfig media = new MinioProfileConfig();

        /**
         * Backup profile configuration for MinIO.
         */
        @NotNull
        public MinioProfileConfig backup = new MinioProfileConfig();
    }

    /**
     * MinIO profile-specific configuration.
     */
    @Getter
    public static class MinioProfileConfig {
        /**
         * Profile-specific MinIO URL (defaults to main URL).
         */
        public String url = "${minio.url:http://localhost:9001}";

        /**
         * Profile-specific access key (defaults to main access key).
         */
        public String accessKey = "${minio.access-key:minioadmin}";

        /**
         * Profile-specific secret key (defaults to main secret key).
         */
        public String secretKey = "${minio.secret-key:minioadmin}";
    }

    /**
     * Quarkus application configuration.
     */
    @Getter
    public static class QuarkusApplicationConfig {
        /**
         * Application name.
         */
        @NotBlank
        public String name;
    }

    /**
     * Quarkus HTTP configuration (mapped from standard Quarkus properties).
     */
    @Getter
    public static class QuarkusHttpConfig {
        // This class is mapped to quarkus.http.* properties
        // Add actual Quarkus HTTP properties here if needed
    }

    /**
     * Quarkus Hibernate ORM configuration.
     */
    @Getter
    public static class QuarkusHibernateConfig {
        /**
         * Database configuration for Hibernate.
         */
        @NotNull
        public HibernateDatabaseConfig database = new HibernateDatabaseConfig();
    }

    /**
     * Hibernate database configuration.
     */
    @Getter
    public static class HibernateDatabaseConfig {
        /**
         * Default schema for database operations.
         */
        @NotBlank
        public String defaultSchema;
    }

    // ==================== PRODUCERS ====================

    /**
     * Produces a MeterFilter for enabling histogram percentiles on HTTP server requests.
     * Uses the configured histogram percentiles from observability.metrics.histogramPercentiles.
     *
     * @return MeterFilter configured with histogram percentiles
     */
    @Produces
    @ApplicationScoped
    public MeterFilter enableHistogram() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                if (id.getName().startsWith("http.server.requests")) {
                    String[] percentileStrings = observability.metrics.histogramPercentiles.split(",");
                    double[] percentiles = new double[percentileStrings.length];
                    for (int i = 0; i < percentileStrings.length; i++) {
                        percentiles[i] = Double.parseDouble(percentileStrings[i].trim());
                    }

                    return DistributionStatisticConfig.builder()
                            .percentiles(percentiles)
                            .build()
                            .merge(config);
                }
                return config;
            }
        };
    }
}
