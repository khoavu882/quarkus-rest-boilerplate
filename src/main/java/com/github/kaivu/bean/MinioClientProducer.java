package com.github.kaivu.bean;

import com.github.kaivu.annotations.MinioProfile;
import com.github.kaivu.config.ConfigsProvider;
import io.minio.MinioClient;
import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.Produces;

public class MinioClientProducer {

    @Produces
    @Default
    @MinioProfile("core")
    public MinioClient coreMinioClient() {
        return MinioClient.builder()
                .endpoint(ConfigsProvider.MINIO_URL)
                .credentials(ConfigsProvider.MINIO_ACCESS_KEY, ConfigsProvider.MINIO_SECRET_KEY)
                .build();
    }

    @Produces
    @MinioProfile("web")
    public MinioClient webMinioClient() {
        return MinioClient.builder()
                .endpoint(ConfigsProvider.MINIO_URL)
                .credentials(ConfigsProvider.MINIO_ACCESS_KEY, ConfigsProvider.MINIO_SECRET_KEY)
                .build();
    }
}
