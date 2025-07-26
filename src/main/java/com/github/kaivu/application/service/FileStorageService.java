package com.github.kaivu.application.service;

import com.github.kaivu.configuration.minio.MinioManager;
import com.github.kaivu.configuration.minio.MinioProfile;
import com.github.kaivu.configuration.minio.MinioProfileType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Example service demonstrating how different services can inject MinioManager
 * with different profiles using @MinioProfile annotation
 */
@Slf4j
@ApplicationScoped
public class FileStorageService {

    // Web assets service uses WEB profile
    @Inject
    @MinioProfile(MinioProfileType.WEB)
    MinioManager webMinioManager;

    // Backup service uses BACKUP profile
    @Inject
    @MinioProfile(MinioProfileType.BACKUP)
    MinioManager backupMinioManager;

    // Default service uses CORE profile
    @Inject
    @MinioProfile(MinioProfileType.CORE)
    MinioManager coreMinioManager;

    /**
     * Store web asset using WEB profile
     */
    public Uni<Void> storeWebAsset(
            String bucketName, String fileName, InputStream content, String contentType, long size) {
        log.info("Storing web asset: {} in bucket: {}", fileName, bucketName);
        return webMinioManager.uploadObject(bucketName, fileName, content, contentType, size);
    }

    /**
     * Backup file using BACKUP profile
     */
    public Uni<Void> backupFile(
            String bucketName, String fileName, InputStream content, String contentType, long size) {
        log.info("Backing up file: {} to bucket: {}", fileName, bucketName);
        return backupMinioManager.uploadObject(bucketName, fileName, content, contentType, size);
    }

    /**
     * Store regular file using CORE profile
     */
    public Uni<Void> storeFile(String bucketName, String fileName, InputStream content, String contentType, long size) {
        log.info("Storing file: {} in bucket: {}", fileName, bucketName);
        return coreMinioManager.uploadObject(bucketName, fileName, content, contentType, size);
    }

    /**
     * Get web asset using WEB profile
     */
    public Uni<InputStream> getWebAsset(String bucketName, String fileName) {
        log.info("Retrieving web asset: {} from bucket: {}", fileName, bucketName);
        return webMinioManager.getObject(bucketName, fileName);
    }

    /**
     * Check if backup exists using BACKUP profile
     */
    public Uni<Boolean> backupExists(String bucketName, String fileName) {
        log.info("Checking backup exists: {} in bucket: {}", fileName, bucketName);
        return backupMinioManager.objectExists(bucketName, fileName);
    }
}
