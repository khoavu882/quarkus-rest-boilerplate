package com.github.kaivu.services;

import io.minio.errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Service Interface for managing {@link MinioService}.
 */
public interface MinioService {

    /**
     * Get Object form MinIO.
     *
     * @param bucketName the arg of object.
     * @param objectName the arg of object.
     * @param offset the arg of object.
     * @param length the arg of object.
     * @return stream.
     */
    InputStream getObject(String bucketName, String objectName, long offset, long length)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException;

    /**
     * Get Object form MinIO. Helper method to get size of the object (video)
     *
     * @param bucketName the arg of object.
     * @param objectName the arg of object.
     * @return stream.
     */
    long getSize(String bucketName, String objectName)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException;
}
