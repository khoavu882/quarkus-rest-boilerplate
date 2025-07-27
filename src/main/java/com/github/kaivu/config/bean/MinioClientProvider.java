package com.github.kaivu.config.bean;

import com.github.kaivu.config.ConfigsProvider;
import com.github.kaivu.config.minio.MinioProfile;
import com.github.kaivu.config.minio.MinioProfileType;
import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * MinIO client producer for different profiles
 */
@Slf4j
@ApplicationScoped
public class MinioClientProvider {

    @Produces
    @MinioProfile(MinioProfileType.CORE)
    @ApplicationScoped
    public MinioClient coreMinioClient() {
        log.info("Creating CORE MinIO client");
        return MinioClient.builder()
                .endpoint(ConfigsProvider.MINIO_URL)
                .credentials(ConfigsProvider.MINIO_ACCESS_KEY, ConfigsProvider.MINIO_SECRET_KEY)
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.WEB)
    @ApplicationScoped
    public MinioClient webMinioClient() {
        log.info("Creating WEB MinIO client");
        String webUrl = ConfigProvider.getConfig()
                .getOptionalValue("minio.web.url", String.class)
                .orElse(ConfigsProvider.MINIO_URL);
        String webAccessKey = ConfigProvider.getConfig()
                .getOptionalValue("minio.web.access-key", String.class)
                .orElse(ConfigsProvider.MINIO_ACCESS_KEY);
        String webSecretKey = ConfigProvider.getConfig()
                .getOptionalValue("minio.web.secret-key", String.class)
                .orElse(ConfigsProvider.MINIO_SECRET_KEY);

        return MinioClient.builder()
                .endpoint(webUrl)
                .credentials(webAccessKey, webSecretKey)
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.MEDIA)
    @ApplicationScoped
    public MinioClient mediaMinioClient() {
        log.info("Creating MEDIA MinIO client");
        String mediaUrl = ConfigProvider.getConfig()
                .getOptionalValue("minio.media.url", String.class)
                .orElse(ConfigsProvider.MINIO_URL);
        String mediaAccessKey = ConfigProvider.getConfig()
                .getOptionalValue("minio.media.access-key", String.class)
                .orElse(ConfigsProvider.MINIO_ACCESS_KEY);
        String mediaSecretKey = ConfigProvider.getConfig()
                .getOptionalValue("minio.media.secret-key", String.class)
                .orElse(ConfigsProvider.MINIO_SECRET_KEY);

        return MinioClient.builder()
                .endpoint(mediaUrl)
                .credentials(mediaAccessKey, mediaSecretKey)
                .build();
    }

    @Produces
    @MinioProfile(MinioProfileType.BACKUP)
    @ApplicationScoped
    public MinioClient backupMinioClient() {
        log.info("Creating BACKUP MinIO client");
        String backupUrl = ConfigProvider.getConfig()
                .getOptionalValue("minio.backup.url", String.class)
                .orElse(ConfigsProvider.MINIO_URL);
        String backupAccessKey = ConfigProvider.getConfig()
                .getOptionalValue("minio.backup.access-key", String.class)
                .orElse(ConfigsProvider.MINIO_ACCESS_KEY);
        String backupSecretKey = ConfigProvider.getConfig()
                .getOptionalValue("minio.backup.secret-key", String.class)
                .orElse(ConfigsProvider.MINIO_SECRET_KEY);

        return MinioClient.builder()
                .endpoint(backupUrl)
                .credentials(backupAccessKey, backupSecretKey)
                .build();
    }
}
