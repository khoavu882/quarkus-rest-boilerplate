package com.github.kaivu.bean;

import com.github.kaivu.annotations.MinioProfile;
import com.github.kaivu.services.MinioService;
import com.github.kaivu.services.impl.MinioServiceImpl;
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
