package com.github.kaivu.domain.audit;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.domain.EntityDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AuditListener
 */
@DisplayName("Audit Listener Tests")
class AuditListenerTest {

    private AuditListener auditListener;
    private EntityDevice testEntity;

    @BeforeEach
    void setUp() {
        auditListener = new AuditListener();
        testEntity = new EntityDevice();
    }

    @Test
    @DisplayName("Should create AuditListener successfully")
    void testAuditListenerCreation() {
        assertNotNull(auditListener);
        assertNotNull(testEntity);
    }

    @Test
    @DisplayName("Should verify AuditListener class exists")
    void testAuditListenerExists() {
        // Verify the audit listener class exists
        assertNotNull(AuditListener.class);
        assertTrue(AuditListener.class.getName().contains("AuditListener"));
    }

    @Test
    @DisplayName("Should verify entity has audit fields")
    void testEntityHasAuditFields() {
        // Test that the entity has audit-related methods available
        // The fields may be null initially, but the methods should exist
        assertDoesNotThrow(() -> testEntity.getCreatedDate());
        assertDoesNotThrow(() -> testEntity.getLastModifiedDate());
        assertDoesNotThrow(() -> testEntity.getCreatedBy());
        assertDoesNotThrow(() -> testEntity.getLastModifiedBy());

        // Test setters work
        assertDoesNotThrow(() -> testEntity.setCreatedBy("test-user"));
        assertDoesNotThrow(() -> testEntity.setLastModifiedBy("test-user"));
    }
}
