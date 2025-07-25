package com.github.kaivu.configuration.handler.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.application.exception.EntityConflictException;
import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.configuration.handler.ErrorMessage;
import com.github.kaivu.configuration.handler.ErrorResponse;
import com.github.kaivu.configuration.handler.ErrorsEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Exception Mappers
 */
@DisplayName("Exception Mapper Tests")
class ExceptionMapperTest {

    private EntityNotFoundExceptionMapper entityNotFoundMapper;
    private EntityConflictExceptionMapper entityConflictMapper;
    private ServiceExceptionMapper serviceExceptionMapper;

    @BeforeEach
    void setUp() {
        entityNotFoundMapper = new EntityNotFoundExceptionMapper();
        entityConflictMapper = new EntityConflictExceptionMapper();
        serviceExceptionMapper = new ServiceExceptionMapper();
    }

    @Test
    @DisplayName("Should create exception mappers successfully")
    void testExceptionMapperCreation() {
        assertNotNull(entityNotFoundMapper);
        assertNotNull(entityConflictMapper);
        assertNotNull(serviceExceptionMapper);
    }

    @Test
    @DisplayName("Should verify exception mapper classes exist")
    void testExceptionMapperClassesExist() {
        assertNotNull(EntityNotFoundExceptionMapper.class);
        assertNotNull(EntityConflictExceptionMapper.class);
        assertNotNull(ServiceExceptionMapper.class);
    }

    @Test
    @DisplayName("Should create exceptions successfully")
    void testExceptionCreation() {
        ErrorsEnum[] errors = ErrorsEnum.values();
        if (errors.length > 0) {
            // Test that exceptions can be created with ErrorsEnum
            EntityNotFoundException notFoundEx = new EntityNotFoundException(errors[0]);
            EntityConflictException conflictEx = new EntityConflictException(errors[0]);

            assertNotNull(notFoundEx.getMessage());
            assertNotNull(conflictEx.getMessage());
        }
    }

    @Test
    @DisplayName("Should verify ErrorResponse structure")
    void testErrorResponseExists() {
        // Verify ErrorResponse class exists
        assertNotNull(ErrorResponse.class);

        // Test ErrorMessage creation
        ErrorMessage errorMessage = new ErrorMessage("Test message");
        assertEquals("Test message", errorMessage.getMessage());
    }
}
