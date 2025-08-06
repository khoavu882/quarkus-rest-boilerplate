package com.github.kaivu.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Configuration provider using ConfigMapping pattern for type-safe configuration
 * Follows Quarkus 3.x best practices for configuration management
 */
@ApplicationScoped
public class ConfigsProvider {

    @Inject
    AppConfig appConfig;

    @Inject
    HttpConfig httpConfig;

    @Inject
    DatabaseConfig databaseConfig;

    @Inject
    MinioConfig minioConfig;

    // Deprecated static methods - kept for backward compatibility
    // TODO: Remove in next major version and use injected configs instead
    @Deprecated(forRemoval = true)
    public static String getAppNameStatic() {
        return System.getProperty("quarkus.application.name", "demo-service");
    }

    @Deprecated(forRemoval = true)
    public static Boolean isCompressionEnabledStatic() {
        return Boolean.valueOf(System.getProperty("quarkus.http.enable-compression", "true"));
    }

    // Type-safe configuration mappings
    @ConfigMapping(prefix = "quarkus.application")
    public interface AppConfig {
        @WithDefault("demo-service")
        String name();
    }

    @ConfigMapping(prefix = "quarkus.http")
    public interface HttpConfig {
        @WithName("enable-compression")
        @WithDefault("true")
        boolean enableCompression();

        @WithName("auth-logging")
        @WithDefault("true")
        boolean enableAuthLogging();
    }

    @ConfigMapping(prefix = "quarkus.hibernate-orm.database")
    public interface DatabaseConfig {
        @WithName("default-schema")
        @WithDefault("sch_local")
        String defaultSchema();
    }

    @ConfigMapping(prefix = "minio")
    public interface MinioConfig {
        @WithDefault("http://localhost:9001")
        String url();

        @WithName("access-key")
        @WithDefault("minioadmin")
        String accessKey();

        @WithName("secret-key")
        @WithDefault("minioadmin")
        String secretKey();

        // Profile-specific configurations
        WebConfig web();

        MediaConfig media();

        BackupConfig backup();

        interface WebConfig {
            @WithDefault("${minio.url}")
            String url();

            @WithName("access-key")
            @WithDefault("${minio.access-key}")
            String accessKey();

            @WithName("secret-key")
            @WithDefault("${minio.secret-key}")
            String secretKey();
        }

        interface MediaConfig {
            @WithDefault("${minio.url}")
            String url();

            @WithName("access-key")
            @WithDefault("${minio.access-key}")
            String accessKey();

            @WithName("secret-key")
            @WithDefault("${minio.secret-key}")
            String secretKey();
        }

        interface BackupConfig {
            @WithDefault("${minio.url}")
            String url();

            @WithName("access-key")
            @WithDefault("${minio.access-key}")
            String accessKey();

            @WithName("secret-key")
            @WithDefault("${minio.secret-key}")
            String secretKey();
        }
    }

    // Getters for type-safe access
    public String getAppName() {
        return appConfig.name();
    }

    public boolean isCompressionEnabled() {
        return httpConfig.enableCompression();
    }

    public boolean isAuthLoggingEnabled() {
        return httpConfig.enableAuthLogging();
    }

    public String getDatabaseSchema() {
        return databaseConfig.defaultSchema();
    }

    public String getMinioUrl() {
        return minioConfig.url();
    }

    public String getMinioAccessKey() {
        return minioConfig.accessKey();
    }

    public String getMinioSecretKey() {
        return minioConfig.secretKey();
    }
}
