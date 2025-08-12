package com.github.kaivu.config.bean;

import com.github.kaivu.config.MinioConfiguration;
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
    MinioConfiguration config;

    @Produces
    @MinioProfile(MinioProfileType.CORE)
    @ApplicationScoped
    public MinioClient coreMinioClient() {
        log.info("Creating CORE MinIO client with endpoint: {}", config.url());

        if (config.url() == null || config.url().isBlank()) {
            throw new IllegalStateException("MinIO URL is required for CORE profile");
        }
        if (config.accessKey() == null || config.accessKey().isBlank()) {
            throw new IllegalStateException("MinIO access key is required for CORE profile");
        }
        if (config.secretKey() == null || config.secretKey().isBlank()) {
            throw new IllegalStateException("MinIO secret key is required for CORE profile");
        }

        return MinioClient.builder()
                .endpoint(config.url())
                .credentials(config.accessKey(), config.secretKey())
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.WEB)
    @ApplicationScoped
    public MinioClient webMinioClient() {
        log.info("Creating WEB MinIO client");
        return MinioClient.builder()
                .endpoint(config.web().url())
                .credentials(config.web().accessKey(), config.web().secretKey())
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.MEDIA)
    @ApplicationScoped
    public MinioClient mediaMinioClient() {
        log.info("Creating MEDIA MinIO client");
        return MinioClient.builder()
                .endpoint(config.media().url())
                .credentials(config.media().accessKey(), config.media().secretKey())
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.BACKUP)
    @ApplicationScoped
    public MinioClient backupMinioClient() {
        log.info("Creating BACKUP MinIO client");
        return MinioClient.builder()
                .endpoint(config.backup().url())
                .credentials(config.backup().accessKey(), config.backup().secretKey())
                .build();
    }
}
