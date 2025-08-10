package com.github.kaivu.config.bean;

import com.github.kaivu.config.ApplicationConfiguration;
import com.github.kaivu.config.minio.MinioProfile;
import com.github.kaivu.config.minio.MinioProfileType;
import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO client producer for different profiles
 */
@Slf4j
@ApplicationScoped
public class MinioClientProvider {

    @Inject
    ApplicationConfiguration config;

    @Produces
    @MinioProfile(MinioProfileType.CORE)
    @ApplicationScoped
    public MinioClient coreMinioClient() {
        log.info("Creating CORE MinIO client with endpoint: {}", config.minio.url);

        if (config.minio.url == null || config.minio.url.isBlank()) {
            throw new IllegalStateException("MinIO URL is required for CORE profile");
        }
        if (config.minio.accessKey == null || config.minio.accessKey.isBlank()) {
            throw new IllegalStateException("MinIO access key is required for CORE profile");
        }
        if (config.minio.secretKey == null || config.minio.secretKey.isBlank()) {
            throw new IllegalStateException("MinIO secret key is required for CORE profile");
        }

        return MinioClient.builder()
                .endpoint(config.minio.url)
                .credentials(config.minio.accessKey, config.minio.secretKey)
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.WEB)
    @ApplicationScoped
    public MinioClient webMinioClient() {
        log.info("Creating WEB MinIO client");
        return MinioClient.builder()
                .endpoint(config.minio.web.url)
                .credentials(config.minio.web.accessKey, config.minio.web.secretKey)
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.MEDIA)
    @ApplicationScoped
    public MinioClient mediaMinioClient() {
        log.info("Creating MEDIA MinIO client");
        return MinioClient.builder()
                .endpoint(config.minio.media.url)
                .credentials(config.minio.media.accessKey, config.minio.media.secretKey)
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.BACKUP)
    @ApplicationScoped
    public MinioClient backupMinioClient() {
        log.info("Creating BACKUP MinIO client");
        return MinioClient.builder()
                .endpoint(config.minio.backup.url)
                .credentials(config.minio.backup.accessKey, config.minio.backup.secretKey)
                .build();
    }
}
