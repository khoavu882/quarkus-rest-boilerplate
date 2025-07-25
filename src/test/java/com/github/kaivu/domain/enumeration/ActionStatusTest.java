package com.github.kaivu.domain.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ActionStatus enumeration
 */
@DisplayName("ActionStatus Enumeration Tests")
class ActionStatusTest {

    @Test
    @DisplayName("Should have correct enum values")
    void testEnumValues() {
        ActionStatus[] values = ActionStatus.values();

        assertEquals(3, values.length);
        assertEquals(ActionStatus.ACTIVATED, values[0]);
        assertEquals(ActionStatus.DEACTIVATED, values[1]);
        assertEquals(ActionStatus.DELETED, values[2]);
    }

    @Test
    @DisplayName("Should convert string to enum correctly")
    void testValueOf() {
        assertEquals(ActionStatus.ACTIVATED, ActionStatus.valueOf("ACTIVATED"));
        assertEquals(ActionStatus.DEACTIVATED, ActionStatus.valueOf("DEACTIVATED"));
        assertEquals(ActionStatus.DELETED, ActionStatus.valueOf("DELETED"));
    }

    @Test
    @DisplayName("Should throw exception for invalid enum value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ActionStatus.valueOf("INVALID_STATUS"));
    }

    @Test
    @DisplayName("Should return correct string representation")
    void testToString() {
        assertEquals("ACTIVATED", ActionStatus.ACTIVATED.toString());
        assertEquals("DEACTIVATED", ActionStatus.DEACTIVATED.toString());
        assertEquals("DELETED", ActionStatus.DELETED.toString());
    }

    @Test
    @DisplayName("Should have correct ordinal values")
    void testOrdinal() {
        assertEquals(0, ActionStatus.ACTIVATED.ordinal());
        assertEquals(1, ActionStatus.DEACTIVATED.ordinal());
        assertEquals(2, ActionStatus.DELETED.ordinal());
    }
}
