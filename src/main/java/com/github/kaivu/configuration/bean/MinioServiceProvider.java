package com.github.kaivu.configuration.bean;

import com.github.kaivu.adapter.out.client.MinioService;
import com.github.kaivu.adapter.out.client.impl.MinioServiceImpl;
import com.github.kaivu.configuration.annotations.MinioProfile;
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
