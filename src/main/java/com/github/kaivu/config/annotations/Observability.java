package com.github.kaivu.config.annotations;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interceptor binding for comprehensive observability including metrics, tracing,
 * and structured logging with context propagation.
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Observability {

    /**
     * Operation name for metrics and tracing
     */
    String value() default "";

    /**
     * Service layer for categorization
     */
    String layer() default "";

    /**
     * Enable metrics collection
     */
    boolean metrics() default true;

    /**
     * Enable execution time logging
     */
    boolean logging() default true;
}
