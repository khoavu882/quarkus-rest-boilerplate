package com.github.kaivu.configuration.bean;

import com.github.kaivu.adapter.out.client.MinioHelper;
import com.github.kaivu.configuration.minio.MinioManager;
import com.github.kaivu.configuration.minio.MinioProfile;
import com.github.kaivu.configuration.minio.MinioProfileType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO manager provider for different profiles
 * Produces profile-specific MinioManager instances that consumers can inject with @MinioProfile
 */
@Slf4j
@ApplicationScoped
public class MinioManagerProvider {

    /**
     * Core MinIO Manager (uses CORE profile MinioHelper)
     */
    @Produces
    @Singleton
    @MinioProfile(MinioProfileType.CORE)
    public MinioManager coreMinioManager(@MinioProfile(MinioProfileType.CORE) MinioHelper minioHelper) {
        log.info("Creating CORE MinIO manager");
        return new MinioManager(minioHelper);
    }

    /**
     * Web MinIO Manager (uses WEB profile MinioHelper)
     */
    @Produces
    @Singleton
    @MinioProfile(MinioProfileType.WEB)
    public MinioManager webMinioManager(@MinioProfile(MinioProfileType.WEB) MinioHelper minioHelper) {
        log.info("Creating WEB MinIO manager");
        return new MinioManager(minioHelper);
    }

    /**
     * Media MinIO Manager (uses MEDIA profile MinioHelper)
     */
    @Produces
    @Singleton
    @MinioProfile(MinioProfileType.MEDIA)
    public MinioManager mediaMinioManager(@MinioProfile(MinioProfileType.MEDIA) MinioHelper minioHelper) {
        log.info("Creating MEDIA MinIO manager");
        return new MinioManager(minioHelper);
    }

    /**
     * Backup MinIO Manager (uses BACKUP profile MinioHelper)
     */
    @Produces
    @Singleton
    @MinioProfile(MinioProfileType.BACKUP)
    public MinioManager backupMinioManager(@MinioProfile(MinioProfileType.BACKUP) MinioHelper minioHelper) {
        log.info("Creating BACKUP MinIO manager");
        return new MinioManager(minioHelper);
    }
}
