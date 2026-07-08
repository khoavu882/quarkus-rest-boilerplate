package com.github.kaivu.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * MinIO configuration interface using SmallRye Config best practices.
 */
@ConfigMapping(prefix = "minio")
public interface MinioConfiguration {

    /**
     * MinIO server URL
     */
    String url();

    /**
     * MinIO access key
     */
    @WithName("access-key")
    String accessKey();

    /**
     * MinIO secret key
     */
    @WithName("secret-key")
    String secretKey();

    /**
     * Web profile configuration for MinIO
     */
    @WithName("web")
    MinioProfileConfig web();

    /**
     * Media profile configuration for MinIO
     */
    @WithName("media")
    MinioProfileConfig media();

    /**
     * Backup profile configuration for MinIO
     */
    @WithName("backup")
    MinioProfileConfig backup();

    /**
     * MinIO profile-specific configuration
     */
    interface MinioProfileConfig {
        /**
         * Profile-specific MinIO URL (defaults to main URL)
         */
        @WithDefault("${minio.url:http://localhost:9001}")
        String url();

        /**
         * Profile-specific access key (defaults to main access key)
         */
        @WithName("access-key")
        @WithDefault("${minio.access-key:minioadmin}")
        String accessKey();

        /**
         * Profile-specific secret key (defaults to main secret key)
         */
        @WithName("secret-key")
        @WithDefault("${minio.secret-key:minioadmin}")
        String secretKey();
    }
}
