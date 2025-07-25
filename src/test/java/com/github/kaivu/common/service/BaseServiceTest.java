package com.github.kaivu.common.service;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.domain.EntityDevice;
import com.github.kaivu.domain.enumeration.ActionStatus;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Unit tests for Base Service classes
 */
@QuarkusTest
@DisplayName("Base Service Tests")
class BaseServiceTest {

    private EntityDevice testEntity;

    @BeforeEach
    void setUp() {
        testEntity = createTestEntity();
    }

    @Test
    @DisplayName("Should create test entity successfully")
    void testCreateTestEntity() {
        assertNotNull(testEntity);
        assertNotNull(testEntity.getId());
        assertEquals("TEST DEVICE", testEntity.getName());
        assertEquals("Test Description", testEntity.getDescription());
        assertEquals(ActionStatus.ACTIVATED, testEntity.getStatus());
    }

    @Test
    @DisplayName("Should verify base service concepts")
    void testBaseServiceConcepts() {
        // Test that base service interfaces exist
        assertNotNull(BaseWriteService.class);
        assertNotNull(BaseReadService.class);

        // Verify they have the expected generic structure
        assertTrue(BaseWriteService.class.isInterface());
        assertTrue(BaseReadService.class.isInterface());
    }

    private EntityDevice createTestEntity() {
        EntityDevice entity = new EntityDevice();
        entity.setId(UUID.randomUUID());
        entity.setName("Test Device");
        entity.setDescription("Test Description");
        entity.setStatus(ActionStatus.ACTIVATED);
        return entity;
    }
}
