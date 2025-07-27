package com.github.kaivu.config.redis;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simplified Redis profile qualifier for dependency injection
 *
 * Usage:
 * - @RedisProfile(RedisProfileType.DEFAULT) - uses default Redis connection
 * - @RedisProfile(RedisProfileType.DEMO) - uses demo Redis connection
 * - @RedisProfile(RedisProfileType.CACHE) - uses cache-specific Redis connection
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
public @interface RedisProfile {
    RedisProfileType value() default RedisProfileType.DEFAULT;
}
