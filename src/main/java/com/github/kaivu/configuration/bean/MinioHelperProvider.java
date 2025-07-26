package com.github.kaivu.configuration.bean;

import com.github.kaivu.adapter.out.client.MinioHelper;
import com.github.kaivu.adapter.out.client.impl.MinioHelperImpl;
import com.github.kaivu.configuration.minio.MinioProfile;
import com.github.kaivu.configuration.minio.MinioProfileType;
import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Simplified MinIO helper provider using Helper pattern only
 * Follows hexagonal architecture and project standards (same pattern as Redis)
 */
@Slf4j
@ApplicationScoped
public class MinioHelperProvider {

    /**
     * Core MinIO Helper (uses main MinIO configuration)
     */
    @Produces
    @Singleton
    @MinioProfile(MinioProfileType.CORE)
    public MinioHelper coreMinioHelper(@MinioProfile(MinioProfileType.CORE) MinioClient minioClient) {
        log.info("Creating CORE MinIO helper");
        return new MinioHelperImpl(minioClient);
    }

    /**
     * Web MinIO Helper (uses web-specific MinIO configuration)
     */
    @Produces
    @Singleton
    @MinioProfile(MinioProfileType.WEB)
    public MinioHelper webMinioHelper(@MinioProfile(MinioProfileType.WEB) MinioClient minioClient) {
        log.info("Creating WEB MinIO helper");
        return new MinioHelperImpl(minioClient);
    }

    /**
     * Media MinIO Helper (uses media-specific MinIO configuration)
     */
    @Produces
    @Singleton
    @MinioProfile(MinioProfileType.MEDIA)
    public MinioHelper mediaMinioHelper(@MinioProfile(MinioProfileType.MEDIA) MinioClient minioClient) {
        log.info("Creating MEDIA MinIO helper");
        return new MinioHelperImpl(minioClient);
    }

    /**
     * Backup MinIO Helper (uses backup-specific MinIO configuration)
     */
    @Produces
    @Singleton
    @MinioProfile(MinioProfileType.BACKUP)
    public MinioHelper backupMinioHelper(@MinioProfile(MinioProfileType.BACKUP) MinioClient minioClient) {
        log.info("Creating BACKUP MinIO helper");
        return new MinioHelperImpl(minioClient);
    }
}
