package com.github.kaivu.config.annotations;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interceptor binding for repository layer observability including database operation metrics,
 * tenant isolation, connection pool monitoring, and reactive operation support.
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RepositoryObservability {

    /**
     * Operation type for database operations (CRUD, batch, etc.)
     */
    String operationType() default "";

    /**
     * Entity name for database operations
     */
    String entityName() default "";

    /**
     * Enable database metrics collection
     */
    boolean metrics() default true;

    /**
     * Enable connection pool monitoring
     */
    boolean connectionPoolMonitoring() default true;
}
