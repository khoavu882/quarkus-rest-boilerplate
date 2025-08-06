package com.github.kaivu.config.annotations;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Profile-aware qualifier annotation for conditional bean creation
 *
 * This annotation combines multiple profile conditions for better
 * environment-specific bean management.
 *
 * Usage:
 * <pre>
 * {@code @Produces}
 * {@code @ProfileAware(profiles = {"dev", "test"})}
 * {@code @ApplicationScoped}
 * public SomeService developmentService() {
 *     return new MockService();
 * }
 *
 * {@code @Produces}
 * {@code @ProfileAware(profiles = {"prod"})}
 * {@code @ApplicationScoped}
 * public SomeService productionService() {
 *     return new RealService();
 * }
 * </pre>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface ProfileAware {
    /**
     * The profiles for which this bean should be active
     */
    String[] profiles() default {};

    /**
     * Whether this is the default implementation (active when no specific profile matches)
     */
    boolean isDefault() default false;

    /**
     * Optional description for documentation purposes
     */
    String description() default "";
}
