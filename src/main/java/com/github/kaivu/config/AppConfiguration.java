package com.github.kaivu.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Application configuration interface using SmallRye Config best practices.
 * This follows the recommended @ConfigMapping pattern for type-safe configuration.
 */
@ConfigMapping(prefix = "app")
public interface AppConfiguration {

    /**
     * HTTP configuration section
     */
    @WithName("http")
    HttpConfig http();

    /**
     * Cache configuration section
     */
    @WithName("cache")
    CacheConfig cache();

    /**
     * Health configuration section
     */
    @WithName("health")
    HealthConfig health();

    /**
     * Timeout configuration section
     */
    @WithName("timeout")
    TimeoutConfig timeout();

    /**
     * Observability configuration section
     */
    @WithName("observability")
    ObservabilityConfig observability();

    /**
     * Retry configuration section
     */
    @WithName("retry")
    RetryConfig retry();

    /**
     * Database configuration section
     */
    @WithName("database")
    DatabaseConfig database();

    /**
     * Pagination configuration section
     */
    @WithName("pagination")
    PaginationConfig pagination();

    /**
     * I18N configuration section
     */
    @WithName("i18n")
    I18nConfig i18n();

    // ==================== NESTED INTERFACES ====================

    interface HttpConfig {
        @WithName("enable-compression")
        @WithDefault("true")
        boolean enableCompression();

        @WithName("auth-logging")
        @WithDefault("true")
        boolean authLogging();
    }

    interface CacheConfig {
        @WithName("default-ttl-ms")
        @WithDefault("3600000")
        long defaultTtlMs();

        @WithName("default-expire-duration-ms")
        @WithDefault("3600000")
        long defaultExpireDurationMs();

        @WithName("entity-device")
        EntityDeviceConfig entityDevice();

        @WithName("entity-device-details")
        EntityDeviceDetailsConfig entityDeviceDetails();

        @WithName("entity-device-page")
        EntityDevicePageConfig entityDevicePage();

        @WithName("media-file")
        MediaFileConfig mediaFile();

        @WithName("message-interpolator")
        MessageInterpolatorConfig messageInterpolator();

        @WithName("prefix")
        PrefixConfig prefix();

        interface EntityDeviceConfig {
            @WithName("ttl-ms")
            @WithDefault("1800000")
            long ttlMs();
        }

        interface EntityDeviceDetailsConfig {
            @WithName("ttl-ms")
            @WithDefault("3600000")
            long ttlMs();
        }

        interface EntityDevicePageConfig {
            @WithName("ttl-ms")
            @WithDefault("900000")
            long ttlMs();
        }

        interface MediaFileConfig {
            @WithName("ttl-ms")
            @WithDefault("3600000")
            long ttlMs();
        }

        interface MessageInterpolatorConfig {
            @WithName("ttl-ms")
            @WithDefault("3600000")
            long ttlMs();

            @WithName("expire-hours")
            @WithDefault("1")
            long expireHours();

            @WithName("max-size")
            @WithDefault("1000")
            long maxSize();
        }

        interface PrefixConfig {
            @WithName("entity-device")
            @WithDefault("entity_device")
            String entityDevice();

            @WithName("entity-device-details")
            @WithDefault("entity_device_details")
            String entityDeviceDetails();

            @WithName("entity-device-page")
            @WithDefault("entity_device_page")
            String entityDevicePage();

            @WithName("media-file")
            @WithDefault("MediaFile")
            String mediaFile();
        }
    }

    interface HealthConfig {
        @WithName("error-rate-threshold")
        @WithDefault("5.0")
        double errorRateThreshold();

        @WithName("response-time-threshold")
        @WithDefault("1000")
        long responseTimeThreshold();

        @WithName("cache-test-timeout-ms")
        @WithDefault("5000")
        long cacheTestTimeoutMs();

        @WithName("cache-test-ttl-ms")
        @WithDefault("60000")
        long cacheTestTtlMs();
    }

    interface TimeoutConfig {
        @WithName("health-check-ms")
        @WithDefault("10000")
        long healthCheckMs();

        @WithName("cache-operation-ms")
        @WithDefault("5000")
        long cacheOperationMs();
    }

    interface ObservabilityConfig {
        @WithName("metrics")
        MetricsConfig metrics();

        @WithName("health-check-id")
        HealthCheckIdConfig healthCheckId();

        interface MetricsConfig {
            @WithName("histogram-percentiles")
            @WithDefault("0.5,0.95,0.99,0.999")
            String histogramPercentiles();
        }

        interface HealthCheckIdConfig {
            @WithName("timestamp-mod")
            @WithDefault("1000000")
            long timestampMod();
        }
    }

    interface RetryConfig {
        @WithName("max-attempts")
        @WithDefault("3")
        int maxAttempts();

        @WithName("initial-backoff-ms")
        @WithDefault("100")
        long initialBackoffMs();

        @WithName("max-backoff-ms")
        @WithDefault("2000")
        long maxBackoffMs();

        @WithName("cache")
        CacheRetryConfig cache();

        @WithName("default-retry")
        DefaultRetryConfig defaultRetry();

        interface CacheRetryConfig {
            @WithName("max-attempts")
            @WithDefault("3")
            int maxAttempts();

            @WithName("backoff-ms")
            @WithDefault("100")
            long backoffMs();
        }

        interface DefaultRetryConfig {
            @WithName("max-attempts")
            @WithDefault("3")
            int maxAttempts();

            @WithName("backoff-min-ms")
            @WithDefault("100")
            long backoffMinMs();

            @WithName("backoff-max-ms")
            @WithDefault("2000")
            long backoffMaxMs();
        }
    }

    interface DatabaseConfig {
        @WithName("entity")
        EntityConfig entity();

        @WithName("audit")
        AuditConfig audit();

        interface EntityConfig {
            @WithName("name-max-length")
            @WithDefault("500")
            int nameMaxLength();

            @WithName("description-max-length")
            @WithDefault("2000")
            int descriptionMaxLength();
        }

        interface AuditConfig {
            @WithName("user-field-length")
            @WithDefault("50")
            int userFieldLength();

            @WithName("default-user")
            @WithDefault("anonymous")
            String defaultUser();
        }
    }

    interface PaginationConfig {
        @WithName("header")
        HeaderConfig header();

        interface HeaderConfig {
            @WithName("total-count")
            @WithDefault("X-Total-Count")
            String totalCount();

            @WithName("link-format")
            @WithDefault("<{0}>; rel=\"{1}\"")
            String linkFormat();
        }
    }

    interface I18nConfig {
        @WithName("path")
        @WithDefault("i18n")
        String path();

        @WithName("error-messages")
        @WithDefault("i18n/error_messages")
        String errorMessages();

        @WithName("validation-messages")
        @WithDefault("i18n/validation_messages")
        String validationMessages();
    }
}
