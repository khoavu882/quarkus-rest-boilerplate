package com.github.kaivu.adapter.in.rest.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.configuration.annotations.ValidEnumValue;
import com.github.kaivu.domain.enumeration.ActionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for EnumValueValidator
 */
@DisplayName("Enum Value Validator Tests")
class EnumValueValidatorTest {

    private EnumValueValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EnumValueValidator();
    }

    @Test
    @DisplayName("Should validate null values as valid")
    void testValidateNullValue() {
        // Create a mock annotation manually
        ValidEnumValue annotation = new ValidEnumValue() {
            @Override
            public Class<? extends Enum<?>> enumClass() {
                return ActionStatus.class;
            }

            @Override
            public String message() {
                return "Invalid enum value";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ValidEnumValue.class;
            }
        };

        validator.initialize(annotation);

        assertTrue(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Should validate valid enum string")
    void testValidateValidEnumString() {
        ValidEnumValue annotation = new ValidEnumValue() {
            @Override
            public Class<? extends Enum<?>> enumClass() {
                return ActionStatus.class;
            }

            @Override
            public String message() {
                return "Invalid enum value";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ValidEnumValue.class;
            }
        };

        validator.initialize(annotation);

        assertTrue(validator.isValid("ACTIVATED", null));
        assertTrue(validator.isValid("DEACTIVATED", null));
        assertTrue(validator.isValid("DELETED", null));
    }

    @Test
    @DisplayName("Should invalidate invalid enum string")
    void testValidateInvalidEnumString() {
        ValidEnumValue annotation = new ValidEnumValue() {
            @Override
            public Class<? extends Enum<?>> enumClass() {
                return ActionStatus.class;
            }

            @Override
            public String message() {
                return "Invalid enum value";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ValidEnumValue.class;
            }
        };

        validator.initialize(annotation);

        assertFalse(validator.isValid("INVALID_STATUS", null));
        assertFalse(validator.isValid("invalid", null));
        assertFalse(validator.isValid("", null));
    }
}
