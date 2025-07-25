package com.github.kaivu.adapter.in.rest.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * Unit tests for DTO classes
 */
@DisplayName("DTO Tests")
class DTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create CreateEntityDTO with valid data")
    void testCreateEntityDTOValid() {
        CreateEntityDTO dto = new CreateEntityDTO("Test Device", "Test Description");

        Set<ConstraintViolation<CreateEntityDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());

        assertEquals("Test Device", dto.name());
        assertEquals("Test Description", dto.description());
    }

    @Test
    @DisplayName("Should validate CreateEntityDTO with empty name")
    void testCreateEntityDTOEmptyName() {
        CreateEntityDTO dto = new CreateEntityDTO("", "Test Description");

        Set<ConstraintViolation<CreateEntityDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should validate CreateEntityDTO with null name")
    void testCreateEntityDTONullName() {
        CreateEntityDTO dto = new CreateEntityDTO(null, "Test Description");

        Set<ConstraintViolation<CreateEntityDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should create UpdateEntityDTO with valid data")
    void testUpdateEntityDTOValid() {
        UpdateEntityDTO dto = new UpdateEntityDTO("Updated Device", "Updated Description");

        assertEquals("Updated Device", dto.name());
        assertEquals("Updated Description", dto.description());
    }

    @Test
    @DisplayName("Should create EntityDeviceFilters with valid data")
    void testEntityDeviceFilters() {
        EntityDeviceFilters filters = new EntityDeviceFilters();

        assertNotNull(filters);
        // Test that filters object can be created
    }

    @Test
    @DisplayName("Should handle null values in record DTOs")
    void testRecordDTOsWithNullValues() {
        CreateEntityDTO createDto = new CreateEntityDTO(null, null);
        UpdateEntityDTO updateDto = new UpdateEntityDTO(null, null);

        // Records allow null values but validation should catch them
        assertNull(createDto.name());
        assertNull(createDto.description());
        assertNull(updateDto.name());
        assertNull(updateDto.description());
    }
}
