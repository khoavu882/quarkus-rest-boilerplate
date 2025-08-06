package com.github.kaivu.config.annotations;

import com.github.kaivu.adapter.in.rest.validator.EnumValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for enum values
 *
 * Usage:
 * <pre>
 * public class MyDto {
 *     {@code @ValidEnumValue(enumClass = StatusEnum.class, message = "status.invalid")}
 *     private String status;
 * }
 * </pre>
 *
 * Error message pattern should follow: entity.field_error_type
 * Example: "entity_device.status_invalid", "user.role_invalid"
 */
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnumValue {
    String message() default "Invalid target value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * The enum class to validate against
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * Whether null values should be considered valid
     * Default: true (null handling should be done with @NotNull if required)
     */
    boolean allowNull() default true;

    /**
     * Whether to perform case-insensitive matching
     * Default: false (case-sensitive matching)
     */
    boolean ignoreCase() default false;
}
