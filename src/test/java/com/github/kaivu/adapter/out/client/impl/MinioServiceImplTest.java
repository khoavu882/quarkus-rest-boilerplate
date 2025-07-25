package com.github.kaivu.adapter.out.client.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.kaivu.adapter.out.client.MinioService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Test class for MinioServiceImpl
 */
class MinioServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private StatObjectResponse statObjectResponse;

    @Mock
    private GetObjectResponse getObjectResponse;

    private MinioServiceImpl minioService;

    private final String bucketName = "test-bucket";
    private final String objectName = "test-object";
    private final long offset = 0L;
    private final long length = 1024L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        minioService = new MinioServiceImpl(minioClient);
    }

    @Test
    void getObject_ShouldReturnInputStream_WhenValidParameters() throws Exception {
        // Given
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(getObjectResponse);

        // When
        InputStream result = minioService.getObject(bucketName, objectName, offset, length);

        // Then
        assertNotNull(result);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getObject_ShouldThrowMinioException_WhenMinioClientFails() throws Exception {
        // Given
        // Use a simpler runtime exception instead of ErrorResponseException
        RuntimeException runtimeException = new RuntimeException("MinIO operation failed");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(runtimeException);

        // When & Then
        assertThrows(RuntimeException.class, () -> minioService.getObject(bucketName, objectName, offset, length));

        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getObject_ShouldThrowIOException_WhenIOError() throws Exception {
        // Given
        IOException ioException = new IOException("IO error");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(ioException);

        // When & Then
        assertThrows(IOException.class, () -> minioService.getObject(bucketName, objectName, offset, length));

        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getObject_ShouldThrowInvalidKeyException_WhenInvalidKey() throws Exception {
        // Given
        InvalidKeyException invalidKeyException = new InvalidKeyException("Invalid key");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(invalidKeyException);

        // When & Then
        assertThrows(InvalidKeyException.class, () -> minioService.getObject(bucketName, objectName, offset, length));

        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getObject_ShouldThrowNoSuchAlgorithmException_WhenAlgorithmError() throws Exception {
        // Given
        NoSuchAlgorithmException algorithmException = new NoSuchAlgorithmException("Algorithm error");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(algorithmException);

        // When & Then
        assertThrows(
                NoSuchAlgorithmException.class, () -> minioService.getObject(bucketName, objectName, offset, length));

        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getSize_ShouldReturnObjectSize_WhenValidParameters() throws Exception {
        // Given
        long expectedSize = 2048L;
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);
        when(statObjectResponse.size()).thenReturn(expectedSize);

        // When
        long result = minioService.getSize(bucketName, objectName);

        // Then
        assertEquals(expectedSize, result);

        verify(minioClient).statObject(any(StatObjectArgs.class));
        verify(statObjectResponse).size();
    }

    @Test
    void getSize_ShouldThrowMinioException_WhenStatObjectFails() throws Exception {
        // Given
        // Use a simpler runtime exception instead of ErrorResponseException
        RuntimeException runtimeException = new RuntimeException("MinIO stat operation failed");
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(runtimeException);

        // When & Then
        assertThrows(RuntimeException.class, () -> minioService.getSize(bucketName, objectName));

        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    void getSize_ShouldThrowIOException_WhenIOError() throws Exception {
        // Given
        IOException ioException = new IOException("IO error");
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(ioException);

        // When & Then
        assertThrows(IOException.class, () -> minioService.getSize(bucketName, objectName));

        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    void getSize_ShouldThrowInvalidKeyException_WhenInvalidKey() throws Exception {
        // Given
        InvalidKeyException invalidKeyException = new InvalidKeyException("Invalid key");
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(invalidKeyException);

        // When & Then
        assertThrows(InvalidKeyException.class, () -> minioService.getSize(bucketName, objectName));

        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    void getSize_ShouldThrowNoSuchAlgorithmException_WhenAlgorithmError() throws Exception {
        // Given
        NoSuchAlgorithmException algorithmException = new NoSuchAlgorithmException("Algorithm error");
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(algorithmException);

        // When & Then
        assertThrows(NoSuchAlgorithmException.class, () -> minioService.getSize(bucketName, objectName));

        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    void constructor_ShouldCreateInstance_WithMinioClient() {
        // Given & When
        MinioServiceImpl service = new MinioServiceImpl(minioClient);

        // Then
        assertNotNull(service);
        assertInstanceOf(MinioService.class, service);
    }

    @Test
    void getObject_WithZeroOffset_ShouldWork() throws Exception {
        // Given
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(getObjectResponse);

        // When
        InputStream result = minioService.getObject(bucketName, objectName, 0L, length);

        // Then
        assertNotNull(result);
        assertEquals(getObjectResponse, result);
    }

    @Test
    void getObject_WithLargeLength_ShouldWork() throws Exception {
        // Given
        long largeLength = Long.MAX_VALUE;
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(getObjectResponse);

        // When
        InputStream result = minioService.getObject(bucketName, objectName, offset, largeLength);

        // Then
        assertNotNull(result);
        assertEquals(getObjectResponse, result);
    }
}
