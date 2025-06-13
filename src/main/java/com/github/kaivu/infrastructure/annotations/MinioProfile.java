package com.github.kaivu.infrastructure.annotations;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 2/11/25
 * Time: 11:04 AM
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
public @interface MinioProfile {
    String value();
}
