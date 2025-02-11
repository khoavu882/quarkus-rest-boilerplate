package com.github.kaivu.config;

import com.github.kaivu.annotations.MinioServer;
import com.github.kaivu.services.MinioService;
import com.github.kaivu.services.impl.MinioServiceImpl;
import io.minio.MinioClient;
import jakarta.ws.rs.Produces;

public class MinioServiceProvider {
    @Produces
    @MinioServer("core")
    public MinioService coreMinioService(@MinioServer("core") MinioClient minioClient) {
        return new MinioServiceImpl(minioClient);
    }

    @Produces
    @MinioServer("web")
    public MinioService webMinioService(@MinioServer("web") MinioClient minioClient) {
        return new MinioServiceImpl(minioClient);
    }
}
