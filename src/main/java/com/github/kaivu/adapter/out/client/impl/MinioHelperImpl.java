package com.github.kaivu.adapter.out.client.impl;

import com.github.kaivu.adapter.out.client.MinioHelper;
import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.config.handler.ErrorsEnum;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * MinIO Helper implementation with single client per instance
 * This is NOT a CDI bean - instances are created by MinioHelperProvider
 */
@Slf4j
public class MinioHelperImpl implements MinioHelper {

    private final MinioClient minioClient;

    public MinioHelperImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public Uni<InputStream> getObject(String bucketName, String objectName, long offset, long length) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                log.debug(
                        "Getting object with range: bucket={}, object={}, offset={}, length={}",
                        bucketName,
                        objectName,
                        offset,
                        length);

                return minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .offset(offset)
                        .length(length)
                        .build());
            } catch (Exception e) {
                log.error("Error getting object with range: {}", e.getMessage(), e);
                // Throw simple ServiceException without locale - Resource layer will add locale
                throw new ServiceException(ErrorsEnum.SYSTEM_INTERNAL_SERVER_ERROR);
            }
        }));
    }

    @Override
    public Uni<InputStream> getObject(String bucketName, String objectName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                log.debug("Getting complete object: bucket={}, object={}", bucketName, objectName);

                return minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
            } catch (Exception e) {
                log.error("Error getting object: {}", e.getMessage(), e);
                throw new ServiceException(ErrorsEnum.SYSTEM_INTERNAL_SERVER_ERROR);
            }
        }));
    }

    @Override
    public Uni<Long> getObjectSize(String bucketName, String objectName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                log.debug("Getting object size: bucket={}, object={}", bucketName, objectName);

                return minioClient
                        .statObject(StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build())
                        .size();
            } catch (Exception e) {
                log.error("Error getting object size: {}", e.getMessage(), e);
                throw new ServiceException(ErrorsEnum.SYSTEM_INTERNAL_SERVER_ERROR);
            }
        }));
    }

    @Override
    public Uni<Boolean> objectExists(String bucketName, String objectName) {
        return Uni.createFrom().item(() -> {
            try {
                log.debug("Checking object existence: bucket={}, object={}", bucketName, objectName);

                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
                return true;
            } catch (Exception e) {
                log.debug("Object does not exist: bucket={}, object={}", bucketName, objectName);
                return false;
            }
        });
    }

    @Override
    public Uni<Void> uploadObject(
            String bucketName, String objectName, InputStream inputStream, String contentType, long size) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                log.debug("Uploading object: bucket={}, object={}, size={}", bucketName, objectName, size);

                minioClient.putObject(
                        PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(inputStream, size, -1)
                                .contentType(contentType)
                                .build());
                return null;
            } catch (Exception e) {
                log.error("Error uploading object: {}", e.getMessage(), e);
                throw new ServiceException(ErrorsEnum.SYSTEM_INTERNAL_SERVER_ERROR);
            }
        }));
    }

    @Override
    public Uni<Void> deleteObject(String bucketName, String objectName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                log.debug("Deleting object: bucket={}, object={}", bucketName, objectName);

                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
                return null;
            } catch (Exception e) {
                log.error("Error deleting object: {}", e.getMessage(), e);
                throw new ServiceException(ErrorsEnum.SYSTEM_INTERNAL_SERVER_ERROR);
            }
        }));
    }
}
