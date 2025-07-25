package com.github.kaivu.configuration.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Unit tests for ErrorMessage
 */
@DisplayName("ErrorMessage Tests")
class ErrorMessageTest {

    @Test
    @DisplayName("Should create ErrorMessage with constructor")
    void testErrorMessageCreation() {
        String message = "Test error message";

        ErrorMessage errorMessage = new ErrorMessage(message);

        assertEquals(message, errorMessage.getMessage());
        assertNull(errorMessage.getPath());
        assertNull(errorMessage.getErrorKey());
    }

    @Test
    @DisplayName("Should create ErrorMessage with full constructor")
    void testErrorMessageFullConstructor() {
        String path = "/api/test";
        String errorKey = "TEST_ERROR";
        String message = "Test error message";

        ErrorMessage errorMessage = new ErrorMessage(path, errorKey, message);

        assertEquals(path, errorMessage.getPath());
        assertEquals(errorKey, errorMessage.getErrorKey());
        assertEquals(message, errorMessage.getMessage());
    }

    @Test
    @DisplayName("Should handle null values")
    void testErrorMessageWithNullValues() {
        ErrorMessage errorMessage = new ErrorMessage(null);

        assertNull(errorMessage.getMessage());
        assertNull(errorMessage.getPath());
        assertNull(errorMessage.getErrorKey());
    }

    @Test
    @DisplayName("Should set and get values using setters")
    void testErrorMessageSetters() {
        ErrorMessage errorMessage = new ErrorMessage("Initial message");

        errorMessage.setPath("/new/path");
        errorMessage.setErrorKey("NEW_ERROR");
        errorMessage.setMessage("Updated message");

        assertEquals("/new/path", errorMessage.getPath());
        assertEquals("NEW_ERROR", errorMessage.getErrorKey());
        assertEquals("Updated message", errorMessage.getMessage());
    }
}

/**
 * Unit tests for ErrorsEnum
 */
@DisplayName("ErrorsEnum Tests")
class ErrorsEnumTest {

    @Test
    @DisplayName("Should have correct enum values")
    void testEnumValues() {
        ErrorsEnum[] values = ErrorsEnum.values();

        assertTrue(values.length > 0);
        // Verify some expected error types exist
        boolean hasSystemError = false;
        boolean hasEntityError = false;

        for (ErrorsEnum error : values) {
            if (error.name().contains("SYSTEM")) {
                hasSystemError = true;
            }
            if (error.name().contains("ENTITY")) {
                hasEntityError = true;
            }
        }

        assertTrue(hasSystemError || hasEntityError, "Should have system or entity related errors");
    }

    @Test
    @DisplayName("Should create ErrorMessage with locale")
    void testWithLocale() {
        ErrorsEnum error = ErrorsEnum.values()[0];

        ErrorsEnum result = error.withLocale(Locale.ENGLISH);

        assertNotNull(result);
        assertNotNull(result.getMessage());
        assertEquals(error, result); // withLocale returns the same enum instance
    }

    @Test
    @DisplayName("Should create ErrorMessage with locale and parameters")
    void testWithLocaleAndParameters() {
        ErrorsEnum error = ErrorsEnum.values()[0];

        ErrorsEnum result = error.withLocale(Locale.ENGLISH, "param1", "param2");

        assertNotNull(result);
        assertNotNull(result.getMessage());
        assertEquals(error, result);
    }

    @Test
    @DisplayName("Should handle null locale")
    void testWithNullLocale() {
        ErrorsEnum error = ErrorsEnum.values()[0];

        ErrorsEnum result = error.withLocale(null);

        assertNotNull(result);
        assertEquals(error, result);
    }
}
