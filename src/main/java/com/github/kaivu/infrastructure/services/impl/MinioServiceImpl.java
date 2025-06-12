package com.github.kaivu.infrastructure.services.impl;

import com.github.kaivu.infrastructure.services.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.*;
import jakarta.ws.rs.ext.Provider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Streaming Service Implement of {@link MinioService}.
 */
@Slf4j
@Provider
@AllArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    /**
     * Get Object form MinIO.
     *
     * @param bucketName the arg of object.
     * @param objectName the arg of object.
     * @param offset     the arg of object.
     * @param length     the arg of object.
     * @return stream.
     */
    @Override
    public InputStream getObject(String bucketName, String objectName, long offset, long length)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        // Stream the requested byte range
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .offset(offset)
                .length(length)
                .build());
    }

    /**
     * Get Object form MinIO. Helper method to get size of the object (video)
     *
     * @param bucketName the arg of object.
     * @param objectName the arg of object.
     * @return stream.
     */
    public long getSize(String bucketName, String objectName)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient
                .statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())
                .size();
    }
}
