package com.github.kaivu.configuration.bean;

import com.github.kaivu.configuration.ConfigsProvider;
import com.github.kaivu.configuration.annotations.MinioProfile;
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
