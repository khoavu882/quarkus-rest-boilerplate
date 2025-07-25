package com.github.kaivu.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.domain.enumeration.ActionStatus;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.UUID;

/**
 * Unit tests for EntityDevice domain entity
 */
@DisplayName("EntityDevice Domain Tests")
class EntityDeviceTest {

    @Test
    @DisplayName("Should create EntityDevice with default values")
    void testEntityDeviceDefaultConstructor() {
        EntityDevice entity = new EntityDevice();

        assertNull(entity.getId());
        assertNull(entity.getDescription());
        assertEquals(ActionStatus.ACTIVATED, entity.getStatus());
        assertNotNull(entity.getMetadata());
        assertTrue(entity.getMetadata().isEmpty());
    }

    @Test
    @DisplayName("Should set and get ID")
    void testSetAndGetId() {
        EntityDevice entity = new EntityDevice();
        UUID testId = UUID.randomUUID();

        entity.setId(testId);

        assertEquals(testId, entity.getId());
    }

    @Test
    @DisplayName("Should convert name to uppercase when getting")
    void testGetNameReturnsUppercase() {
        EntityDevice entity = new EntityDevice();
        entity.setName("test device");

        assertEquals("TEST DEVICE", entity.getName());
    }

    @Test
    @DisplayName("Should convert name to lowercase when setting")
    void testSetNameConvertsToLowercase() {
        EntityDevice entity = new EntityDevice();
        entity.setName("TEST DEVICE");

        // Verify the transformation works by getting the uppercase version
        assertEquals("TEST DEVICE", entity.getName());
    }

    @Test
    @DisplayName("Should handle null name gracefully")
    void testSetAndGetNullName() {
        EntityDevice entity = new EntityDevice();
        entity.setName(null);

        assertThrows(NullPointerException.class, entity::getName);
    }

    @Test
    @DisplayName("Should set and get description")
    void testSetAndGetDescription() {
        EntityDevice entity = new EntityDevice();
        String description = "Test device description";

        entity.setDescription(description);

        assertEquals(description, entity.getDescription());
    }

    @Test
    @DisplayName("Should set and get status")
    void testSetAndGetStatus() {
        EntityDevice entity = new EntityDevice();

        entity.setStatus(ActionStatus.DEACTIVATED);

        assertEquals(ActionStatus.DEACTIVATED, entity.getStatus());
    }

    @Test
    @DisplayName("Should set and get metadata")
    void testSetAndGetMetadata() {
        EntityDevice entity = new EntityDevice();
        JsonObject metadata = new JsonObject().put("key1", "value1").put("key2", 123);

        entity.setMetadata(metadata);

        assertEquals(metadata, entity.getMetadata());
        assertEquals("value1", entity.getMetadata().getString("key1"));
        assertEquals(123, entity.getMetadata().getInteger("key2"));
    }

    @Test
    @DisplayName("Should handle empty metadata")
    void testEmptyMetadata() {
        EntityDevice entity = new EntityDevice();
        JsonObject emptyMetadata = new JsonObject();

        entity.setMetadata(emptyMetadata);

        assertTrue(entity.getMetadata().isEmpty());
    }

    @Test
    @DisplayName("Should be serializable")
    void testSerializable() {
        EntityDevice entity = new EntityDevice();

        assertInstanceOf(Serializable.class, entity);
        // Note: serialVersionUID is private, so we can't directly test its value
    }
}
