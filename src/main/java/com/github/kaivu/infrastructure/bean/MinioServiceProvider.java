package com.github.kaivu.infrastructure.bean;

import com.github.kaivu.infrastructure.annotations.MinioProfile;
import com.github.kaivu.infrastructure.services.MinioService;
import com.github.kaivu.infrastructure.services.impl.MinioServiceImpl;
import io.minio.MinioClient;
import jakarta.ws.rs.Produces;

public class MinioServiceProvider {

    @Produces
    @MinioProfile("core")
    public MinioService coreMinioService(@MinioProfile("core") MinioClient minioClient) {
        return new MinioServiceImpl(minioClient);
    }

    @Produces
    @MinioProfile("web")
    public MinioService webMinioService(@MinioProfile("web") MinioClient minioClient) {
        return new MinioServiceImpl(minioClient);
    }
}
