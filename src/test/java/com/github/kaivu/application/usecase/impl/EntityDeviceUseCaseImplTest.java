package com.github.kaivu.application.usecase.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.application.service.EntityDevicesService;
import com.github.kaivu.configuration.handler.ErrorsEnum;
import com.github.kaivu.domain.EntityDevice;
import com.github.kaivu.domain.enumeration.ActionStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

@QuarkusTest
class EntityDeviceUseCaseImplTest {

    @Inject
    EntityDeviceUseCaseImpl useCase;

    @InjectMock
    EntityDevicesService service;

    private EntityDevice testDevice;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testDevice = new EntityDevice();
        testDevice.setId(testId);
        testDevice.setName("Test Device");
        testDevice.setDescription("Test Description");
        testDevice.setStatus(ActionStatus.ACTIVATED);
    }

    @Test
    @TestTransaction
    void testFindById_Found() {
        // Given
        when(service.findById(testId)).thenReturn(Uni.createFrom().item(Optional.of(testDevice)));

        // When
        var result = useCase.findById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
        assertEquals("Test Device", result.get().getName()); // Use case should preserve original case
    }

    @Test
    @TestTransaction
    void testGetById_Found() {
        // Given
        EntityDevice upperCaseDevice = new EntityDevice();
        upperCaseDevice.setId(testId);
        upperCaseDevice.setName("TEST DEVICE"); // Service returns uppercase
        upperCaseDevice.setDescription("Test Description");
        upperCaseDevice.setStatus(ActionStatus.ACTIVATED);

        when(service.getById(testId)).thenReturn(Uni.createFrom().item(upperCaseDevice));

        // When
        var result = useCase.getById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertEquals(testId, result.getId());
        assertEquals("TEST DEVICE", result.getName()); // Should match service output
    }

    @Test
    @TestTransaction
    void testGetById_NotFound() {
        // Given
        when(service.getById(testId)).thenReturn(
            Uni.createFrom().failure(new EntityNotFoundException(ErrorsEnum.ENTITY_DEVICE_NOT_FOUND))
        );

        // When & Then
        useCase.getById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(EntityNotFoundException.class);
    }

    @Test
    @TestTransaction
    void testPersist() {
        // Given
        when(service.persist(any(EntityDevice.class)))
                .thenReturn(Uni.createFrom().item(testDevice));

        // When
        var result = useCase.persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("Test Device", result.getName());
    }
}
