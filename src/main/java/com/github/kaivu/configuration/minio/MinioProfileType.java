package com.github.kaivu.configuration.minio;

import lombok.Getter;

/**
 * MinIO profile types for different environments and use cases
 */
@Getter
public enum MinioProfileType {
    /**
     * Core application MinIO connection (for main content storage)
     */
    CORE("core"),

    /**
     * Web assets MinIO connection (for static web content)
     */
    WEB("web"),

    /**
     * Media storage MinIO connection (for videos, images, etc.)
     */
    MEDIA("media"),

    /**
     * Backup storage MinIO connection
     */
    BACKUP("backup");

    private final String configKey;

    MinioProfileType(String configKey) {
        this.configKey = configKey;
    }
}
