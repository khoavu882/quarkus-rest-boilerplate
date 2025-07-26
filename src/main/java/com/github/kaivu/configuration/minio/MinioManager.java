package com.github.kaivu.configuration.minio;

import com.github.kaivu.adapter.out.client.MinioHelper;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Simplified MinIO manager using Helper pattern only
 * This is NOT a CDI bean - instances are created by MinioManagerProvider
 * Consumers inject this with @MinioProfile annotation to specify the profile they want
 */
@Slf4j
public class MinioManager {

    private final MinioHelper minioHelper;

    public MinioManager(MinioHelper minioHelper) {
        this.minioHelper = minioHelper;
    }

    // Core MinIO operations - profile is determined by the injected helper
    public Uni<InputStream> getObject(String bucketName, String objectName) {
        return minioHelper.getObject(bucketName, objectName);
    }

    public Uni<InputStream> getObject(String bucketName, String objectName, long offset, long length) {
        return minioHelper.getObject(bucketName, objectName, offset, length);
    }

    public Uni<Long> getObjectSize(String bucketName, String objectName) {
        return minioHelper.getObjectSize(bucketName, objectName);
    }

    public Uni<Boolean> objectExists(String bucketName, String objectName) {
        return minioHelper.objectExists(bucketName, objectName);
    }

    public Uni<Void> uploadObject(
            String bucketName, String objectName, InputStream inputStream, String contentType, long size) {
        return minioHelper.uploadObject(bucketName, objectName, inputStream, contentType, size);
    }

    public Uni<Void> deleteObject(String bucketName, String objectName) {
        return minioHelper.deleteObject(bucketName, objectName);
    }

    /**
     * Generate standardized object path
     */
    public String buildObjectPath(String... pathSegments) {
        return String.join("/", pathSegments);
    }
}
