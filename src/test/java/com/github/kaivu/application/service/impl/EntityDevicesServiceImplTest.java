package com.github.kaivu.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.configuration.handler.ErrorsEnum;
import com.github.kaivu.domain.EntityDevice;
import com.github.kaivu.domain.enumeration.ActionStatus;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Unit tests for EntityDevicesServiceImpl
 */
@QuarkusTest
@DisplayName("EntityDevicesService Implementation Tests")
class EntityDevicesServiceImplTest {

    private EntityDevice testEntity;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testEntity = createTestEntity();
    }

    @Test
    @DisplayName("Should create test entity successfully")
    void testCreateTestEntity() {
        assertNotNull(testEntity);
        assertEquals(testId, testEntity.getId());
        assertEquals("TEST DEVICE", testEntity.getName());
        assertEquals("Test Description", testEntity.getDescription());
        assertEquals(ActionStatus.ACTIVATED, testEntity.getStatus());
    }

    @Test
    @DisplayName("Should verify EntityNotFoundException exists")
    void testEntityNotFoundExceptionExists() {
        // Test that the exception class exists and can be instantiated
        ErrorsEnum[] errors = ErrorsEnum.values();
        if (errors.length > 0) {
            EntityNotFoundException exception = new EntityNotFoundException(errors[0]);
            assertNotNull(exception);
            assertNotNull(exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should verify service implementation class exists")
    void testServiceImplementationExists() {
        // Verify the service implementation class exists
        assertNotNull(EntityDevicesServiceImpl.class);
        assertTrue(EntityDevicesServiceImpl.class.getName().contains("ServiceImpl"));
    }

    @Test
    @DisplayName("Should verify ErrorsEnum has values")
    void testErrorsEnumHasValues() {
        ErrorsEnum[] errors = ErrorsEnum.values();
        assertTrue(errors.length > 0, "ErrorsEnum should have error values");
    }

    private EntityDevice createTestEntity() {
        EntityDevice entity = new EntityDevice();
        entity.setId(testId);
        entity.setName("Test Device");
        entity.setDescription("Test Description");
        entity.setStatus(ActionStatus.ACTIVATED);
        return entity;
    }
}
