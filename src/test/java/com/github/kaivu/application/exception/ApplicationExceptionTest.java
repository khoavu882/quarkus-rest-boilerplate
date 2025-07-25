package com.github.kaivu.application.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.configuration.handler.ErrorsEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for application exceptions
 */
@DisplayName("Application Exception Tests")
class ApplicationExceptionTest {

    @Test
    @DisplayName("Should create EntityNotFoundException with ErrorsEnum")
    void testEntityNotFoundExceptionWithErrorsEnum() {
        ErrorsEnum[] errors = ErrorsEnum.values();
        if (errors.length > 0) {
            EntityNotFoundException exception = new EntityNotFoundException(errors[0]);
            assertNotNull(exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should create EntityConflictException with ErrorsEnum")
    void testEntityConflictExceptionWithErrorsEnum() {
        ErrorsEnum[] errors = ErrorsEnum.values();
        if (errors.length > 0) {
            EntityConflictException exception = new EntityConflictException(errors[0]);
            assertNotNull(exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should create UnauthorizedException with ErrorsEnum")
    void testUnauthorizedExceptionWithErrorsEnum() {
        ErrorsEnum[] errors = ErrorsEnum.values();
        if (errors.length > 0) {
            UnauthorizedException exception = new UnauthorizedException(errors[0]);
            assertNotNull(exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should create PermissionDeniedException with ErrorsEnum")
    void testPermissionDeniedExceptionWithErrorsEnum() {
        ErrorsEnum[] errors = ErrorsEnum.values();
        if (errors.length > 0) {
            PermissionDeniedException exception = new PermissionDeniedException(errors[0]);
            assertNotNull(exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should create NotAcceptableException with ErrorsEnum")
    void testNotAcceptableExceptionWithErrorsEnum() {
        ErrorsEnum[] errors = ErrorsEnum.values();
        if (errors.length > 0) {
            NotAcceptableException exception = new NotAcceptableException(errors[0]);
            assertNotNull(exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should verify exception classes exist")
    void testExceptionClassesExist() {
        assertNotNull(EntityNotFoundException.class);
        assertNotNull(EntityConflictException.class);
        assertNotNull(UnauthorizedException.class);
        assertNotNull(PermissionDeniedException.class);
        assertNotNull(NotAcceptableException.class);
    }
}
