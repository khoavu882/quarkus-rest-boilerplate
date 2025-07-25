package com.github.kaivu.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.domain.enumeration.ActionStatus;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

/**
 * Unit tests for EntityDevice domain object
 */
@DisplayName("EntityDevice Unit Tests")
class EntityDeviceTest {

    private EntityDevice entityDevice;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entityDevice = new EntityDevice();
    }

    @Test
    @DisplayName("Should create entity device with default values")
    void testDefaultConstructor() {
        EntityDevice device = new EntityDevice();

        assertNull(device.getId());
        // getName() will throw NPE if name is null, so check the field directly or set a name first
        device.setName("default");
        assertEquals("DEFAULT", device.getName());
        assertNull(device.getDescription());
        assertEquals(ActionStatus.ACTIVATED, device.getStatus());
        assertNotNull(device.getMetadata());
    }

    @Test
    @DisplayName("Should set and get ID correctly")
    void testIdSetterGetter() {
        entityDevice.setId(testId);

        assertEquals(testId, entityDevice.getId());
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void testNameSetterGetter() {
        String testName = "Test Device Name";
        entityDevice.setName(testName);

        // EntityDevice converts name to lowercase on set and uppercase on get
        assertEquals(testName.toUpperCase(), entityDevice.getName());
    }

    @Test
    @DisplayName("Should handle null name")
    void testNullName() {
        // EntityDevice will throw NPE when setting null name because it calls toLowerCase() on null
        assertThrows(NullPointerException.class, () -> entityDevice.setName(null));
    }

    @Test
    @DisplayName("Should set and get description correctly")
    void testDescriptionSetterGetter() {
        String testDescription = "Test Device Description";
        entityDevice.setDescription(testDescription);

        assertEquals(testDescription, entityDevice.getDescription());
    }

    @Test
    @DisplayName("Should handle null description")
    void testNullDescription() {
        entityDevice.setDescription(null);

        assertNull(entityDevice.getDescription());
    }

    @Test
    @DisplayName("Should set and get status correctly")
    void testStatusSetterGetter() {
        entityDevice.setStatus(ActionStatus.ACTIVATED);

        assertEquals(ActionStatus.ACTIVATED, entityDevice.getStatus());
    }

    @Test
    @DisplayName("Should handle all status values")
    void testAllStatusValues() {
        // Test ACTIVATED
        entityDevice.setStatus(ActionStatus.ACTIVATED);
        assertEquals(ActionStatus.ACTIVATED, entityDevice.getStatus());

        // Test DEACTIVATED
        entityDevice.setStatus(ActionStatus.DEACTIVATED);
        assertEquals(ActionStatus.DEACTIVATED, entityDevice.getStatus());

        // Test DELETED
        entityDevice.setStatus(ActionStatus.DELETED);
        assertEquals(ActionStatus.DELETED, entityDevice.getStatus());
    }

    @Test
    @DisplayName("Should set and get metadata correctly")
    void testMetadataSetterGetter() {
        JsonObject metadata =
                new JsonObject().put("key1", "value1").put("key2", 123).put("key3", true);

        entityDevice.setMetadata(metadata);

        assertEquals(metadata, entityDevice.getMetadata());
        assertEquals("value1", entityDevice.getMetadata().getString("key1"));
        assertEquals(123, entityDevice.getMetadata().getInteger("key2"));
        assertTrue(entityDevice.getMetadata().getBoolean("key3"));
    }

    @Test
    @DisplayName("Should handle null metadata")
    void testNullMetadata() {
        entityDevice.setMetadata(null);

        assertNull(entityDevice.getMetadata());
    }

    @Test
    @DisplayName("Should handle empty metadata")
    void testEmptyMetadata() {
        JsonObject emptyMetadata = new JsonObject();
        entityDevice.setMetadata(emptyMetadata);

        assertEquals(emptyMetadata, entityDevice.getMetadata());
        assertTrue(entityDevice.getMetadata().isEmpty());
    }

    @Test
    @DisplayName("Should maintain object state consistency")
    void testObjectStateConsistency() {
        String deviceName = "Consistent Device";
        String deviceDescription = "Consistent Description";

        entityDevice.setName(deviceName);
        entityDevice.setDescription(deviceDescription);
        entityDevice.setStatus(ActionStatus.DEACTIVATED);

        assertEquals(deviceName.toUpperCase(), entityDevice.getName());
        assertEquals(deviceDescription, entityDevice.getDescription());
        assertEquals(ActionStatus.DEACTIVATED, entityDevice.getStatus());
    }

    @Test
    @DisplayName("Should inherit audit fields from AbstractAuditingEntity")
    void testAuditingInheritance() {
        // Test that EntityDevice inherits audit fields
        Instant now = Instant.now();
        String creator = "test-user";

        entityDevice.setCreatedDate(now);
        entityDevice.setCreatedBy(creator);
        entityDevice.setLastModifiedDate(now);
        entityDevice.setLastModifiedBy(creator);

        assertEquals(now, entityDevice.getCreatedDate());
        assertEquals(creator, entityDevice.getCreatedBy());
        assertEquals(now, entityDevice.getLastModifiedDate());
        assertEquals(creator, entityDevice.getLastModifiedBy());
    }

    @Test
    @DisplayName("Should implement Serializable correctly")
    void testSerializable() {
        assertTrue(entityDevice instanceof java.io.Serializable);
    }

    @Test
    @DisplayName("Should handle large description within size constraints")
    void testLargeDescription() {
        // Test description at boundary (assuming reasonable max size)
        String largeDescription = "A".repeat(1000);
        entityDevice.setDescription(largeDescription);

        assertEquals(largeDescription, entityDevice.getDescription());
    }

    @Test
    @DisplayName("Should handle special characters in name and description")
    void testSpecialCharacters() {
        String specialName = "Device@#$%^&*()";
        String specialDescription = "Description with special chars: @#$%^&*()";

        entityDevice.setName(specialName);
        entityDevice.setDescription(specialDescription);

        assertEquals(specialName.toUpperCase(), entityDevice.getName());
        assertEquals(specialDescription, entityDevice.getDescription());
    }
}
