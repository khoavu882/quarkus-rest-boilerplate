package com.github.kaivu.configuration.minio;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simplified MinIO profile qualifier for dependency injection
 *
 * Usage:
 * - @MinioProfile(MinioProfileType.CORE) - uses core MinIO connection
 * - @MinioProfile(MinioProfileType.WEB) - uses web assets MinIO connection
 * - @MinioProfile(MinioProfileType.MEDIA) - uses media storage MinIO connection
 * - @MinioProfile(MinioProfileType.BACKUP) - uses backup storage MinIO connection
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
public @interface MinioProfile {
    MinioProfileType value() default MinioProfileType.CORE;
}
