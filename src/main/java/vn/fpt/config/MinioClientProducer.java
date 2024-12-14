package vn.fpt.config;

import io.minio.MinioClient;
import io.quarkus.arc.DefaultBean;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;

public class MinioClientProducer {

    @Produces
    @Singleton
    @DefaultBean
    public MinioClient produceMinioClient() {
        // Initialize MinioClient with your configuration
        return MinioClient.builder()
                .endpoint(ConfigsProvider.MINIO_URL)
                .credentials(ConfigsProvider.MINIO_ACCESS_KEY, ConfigsProvider.MINIO_SECRET_KEY)
                .build();
    }
}
