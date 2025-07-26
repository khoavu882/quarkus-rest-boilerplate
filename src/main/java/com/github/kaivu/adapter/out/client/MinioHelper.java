package com.github.kaivu.adapter.out.client;

import io.smallrye.mutiny.Uni;

import java.io.InputStream;

/**
 * MinIO Helper providing high-level storage operations with profile flexibility
 *
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 7/26/25
 * Time: 2:30 PM
 */
public interface MinioHelper {

    /**
     * Get object from MinIO with byte range support
     *
     * @param bucketName the bucket name
     * @param objectName the object name
     * @param offset     the offset to start reading from
     * @param length     the number of bytes to read
     * @return Uni with InputStream of the object content
     */
    Uni<InputStream> getObject(String bucketName, String objectName, long offset, long length);

    /**
     * Get the complete object from MinIO
     *
     * @param bucketName the bucket name
     * @param objectName the object name
     * @return Uni with InputStream of the complete object content
     */
    Uni<InputStream> getObject(String bucketName, String objectName);

    /**
     * Get the size of an object in MinIO
     *
     * @param bucketName the bucket name
     * @param objectName the object name
     * @return Uni with the size of the object in bytes
     */
    Uni<Long> getObjectSize(String bucketName, String objectName);

    /**
     * Check if an object exists in MinIO
     *
     * @param bucketName the bucket name
     * @param objectName the object name
     * @return Uni with true if the object exists, false otherwise
     */
    Uni<Boolean> objectExists(String bucketName, String objectName);

    /**
     * Upload an object to MinIO
     *
     * @param bucketName  the bucket name
     * @param objectName  the object name
     * @param inputStream the input stream containing the object data
     * @param contentType the content type of the object
     * @param size        the size of the object in bytes
     * @return Uni representing completion
     */
    Uni<Void> uploadObject(
            String bucketName, String objectName, InputStream inputStream, String contentType, long size);

    /**
     * Delete an object from MinIO
     *
     * @param bucketName the bucket name
     * @param objectName the object name
     * @return Uni representing completion
     */
    Uni<Void> deleteObject(String bucketName, String objectName);
}
