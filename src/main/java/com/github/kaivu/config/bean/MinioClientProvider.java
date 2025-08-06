package com.github.kaivu.config.bean;

import com.github.kaivu.config.ConfigsProvider;
import com.github.kaivu.config.minio.MinioProfile;
import com.github.kaivu.config.minio.MinioProfileType;
import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO client producer for different profiles using type-safe configuration
 * Follows Quarkus 3.x best practices with ConfigMapping
 */
@Slf4j
@ApplicationScoped
public class MinioClientProvider {

    @Inject
    ConfigsProvider.MinioConfig minioConfig;

    @Produces
    @MinioProfile(MinioProfileType.CORE)
    @ApplicationScoped
    public MinioClient coreMinioClient() {
        log.info("Creating CORE MinIO client with endpoint: {}", minioConfig.url());
        return MinioClient.builder()
                .endpoint(minioConfig.url())
                .credentials(minioConfig.accessKey(), minioConfig.secretKey())
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.WEB)
    @ApplicationScoped
    public MinioClient webMinioClient() {
        log.info(
                "Creating WEB MinIO client with endpoint: {}", minioConfig.web().url());
        return MinioClient.builder()
                .endpoint(minioConfig.web().url())
                .credentials(minioConfig.web().accessKey(), minioConfig.web().secretKey())
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.MEDIA)
    @ApplicationScoped
    public MinioClient mediaMinioClient() {
        log.info(
                "Creating MEDIA MinIO client with endpoint: {}",
                minioConfig.media().url());
        return MinioClient.builder()
                .endpoint(minioConfig.media().url())
                .credentials(
                        minioConfig.media().accessKey(), minioConfig.media().secretKey())
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.BACKUP)
    @ApplicationScoped
    public MinioClient backupMinioClient() {
        log.info(
                "Creating BACKUP MinIO client with endpoint: {}",
                minioConfig.backup().url());
        return MinioClient.builder()
                .endpoint(minioConfig.backup().url())
                .credentials(
                        minioConfig.backup().accessKey(), minioConfig.backup().secretKey())
                .build();
    }
}
