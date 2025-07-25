package com.github.kaivu.adapter.in.rest.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.kaivu.configuration.annotations.ValidEnumValue;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Edge case tests for EnumValueValidator to improve coverage
 */
@DisplayName("EnumValueValidator Edge Cases Tests")
class EnumValueValidatorEdgeCasesTest {

    private EnumValueValidator validator;

    @Mock
    private ValidEnumValue enumValue;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new EnumValueValidator();
    }

    @Test
    @DisplayName("Should handle null annotation gracefully")
    void testInitializeWithNullAnnotation() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> validator.initialize(null));
    }

    @Test
    @DisplayName("Should handle null enum class")
    void testInitializeWithNullEnumClass() {
        // Given
        when(enumValue.enumClass()).thenReturn(null);

        // When & Then - should not throw exception during initialization
        assertDoesNotThrow(() -> validator.initialize(enumValue));

        // Validation should handle null enum class gracefully
        assertFalse(validator.isValid("test", context));
    }

    @Test
    @DisplayName("Should handle null value in validation")
    void testIsValidWithNullValue() {
        // Given
        when(enumValue.enumClass()).thenReturn((Class) TestEnum.class);
        validator.initialize(enumValue);

        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertTrue(result); // null values should be considered valid (let @NotNull handle null validation)
    }

    @Test
    @DisplayName("Should handle empty string value")
    void testIsValidWithEmptyString() {
        // Given
        when(enumValue.enumClass()).thenReturn((Class) TestEnum.class);
        validator.initialize(enumValue);

        // When
        boolean result = validator.isValid("", context);

        // Then - empty string should be invalid for enum validation
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle whitespace-only string value")
    void testIsValidWithWhitespaceString() {
        // Given
        when(enumValue.enumClass()).thenReturn((Class) TestEnum.class);
        validator.initialize(enumValue);

        // When
        boolean result = validator.isValid("   ", context);

        // Then - whitespace should be invalid
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle case sensitivity correctly")
    void testIsValidWithCaseSensitivity() {
        // Given
        when(enumValue.enumClass()).thenReturn((Class) TestEnum.class);
        validator.initialize(enumValue);

        // When & Then
        assertTrue(validator.isValid("VALUE1", context)); // exact match
        assertFalse(validator.isValid("value1", context)); // lowercase should fail
        assertFalse(validator.isValid("Value1", context)); // mixed case should fail
    }

    @Test
    @DisplayName("Should handle different enum values")
    void testIsValidWithDifferentEnumValues() {
        // Given
        when(enumValue.enumClass()).thenReturn((Class) AnotherTestEnum.class);
        validator.initialize(enumValue);

        // When & Then
        assertTrue(validator.isValid("OPTION_A", context)); // from AnotherTestEnum
        assertTrue(validator.isValid("OPTION_B", context)); // from AnotherTestEnum
        assertFalse(validator.isValid("VALUE1", context)); // not in AnotherTestEnum
        assertFalse(validator.isValid("INVALID", context)); // not in any enum
    }

    // Test enums
    enum TestEnum {
        VALUE1,
        VALUE2,
        VALUE3
    }

    enum AnotherTestEnum {
        OPTION_A,
        OPTION_B,
        OPTION_C
    }
}
