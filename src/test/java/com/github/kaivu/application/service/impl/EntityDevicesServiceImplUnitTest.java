package com.github.kaivu.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.kaivu.adapter.out.persistence.EntityDeviceRepository;
import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.domain.EntityDevice;
import com.github.kaivu.domain.enumeration.ActionStatus;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Unit tests for EntityDevicesServiceImpl using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EntityDevicesService Unit Tests")
class EntityDevicesServiceImplUnitTest {

    @Mock
    private EntityDeviceRepository entityDeviceRepository;

    @Mock
    private ContainerRequestContext requestContext;

    @InjectMocks
    private EntityDevicesServiceImpl entityDevicesService;

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

        // Mock the ContainerRequestContext to return a default locale
        when(requestContext.getLanguage()).thenReturn(java.util.Locale.ENGLISH);
    }

    @Test
    @DisplayName("Should find device by ID successfully")
    void testFindById_Success() {
        // Given
        when(entityDeviceRepository.findById(testId))
                .thenReturn(Uni.createFrom().item(Optional.of(testDevice)));

        // When
        var result = entityDevicesService
                .findById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
        verify(entityDeviceRepository).findById(testId);
    }

    @Test
    @DisplayName("Should return empty when device not found by ID")
    void testFindById_NotFound() {
        // Given
        when(entityDeviceRepository.findById(testId))
                .thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        var result = entityDevicesService
                .findById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertTrue(result.isEmpty());
        verify(entityDeviceRepository).findById(testId);
    }

    @Test
    @DisplayName("Should get device by ID successfully")
    void testGetById_Success() {
        // Given
        when(entityDeviceRepository.findById(testId))
                .thenReturn(Uni.createFrom().item(Optional.of(testDevice)));

        // When
        var result = entityDevicesService
                .getById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertEquals(testId, result.getId());
        assertEquals("TEST DEVICE", result.getName()); // EntityDevice converts to uppercase
        verify(entityDeviceRepository).findById(testId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when device not found")
    void testGetById_NotFound() {
        // Given
        when(entityDeviceRepository.findById(testId))
                .thenReturn(Uni.createFrom().item(Optional.empty()));

        // When & Then
        entityDevicesService
                .getById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(EntityNotFoundException.class);

        verify(entityDeviceRepository).findById(testId);
    }

    @Test
    @DisplayName("Should persist single device successfully")
    void testPersist_SingleDevice() {
        // Given
        when(entityDeviceRepository.persist(testDevice))
                .thenReturn(Uni.createFrom().item(testDevice));

        // When
        var result = entityDevicesService
                .persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertEquals(testId, result.getId());
        verify(entityDeviceRepository).persist(testDevice);
    }

    @Test
    @DisplayName("Should persist multiple devices successfully")
    void testPersist_MultipleDevices() {
        // Given
        EntityDevice device2 = new EntityDevice();
        device2.setId(UUID.randomUUID());
        device2.setName("Test Device 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        List<EntityDevice> devices = Arrays.asList(testDevice, device2);
        when(entityDeviceRepository.persist(devices))
                .thenReturn(Uni.createFrom().item(devices));

        // When
        var result = entityDevicesService
                .persist(devices)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertEquals(2, result.size());
        verify(entityDeviceRepository).persist(devices);
    }

    @Test
    @DisplayName("Should update single device successfully")
    void testUpdate_SingleDevice() {
        // Given
        testDevice.setName("Updated Device");
        when(entityDeviceRepository.update(testDevice))
                .thenReturn(Uni.createFrom().item(testDevice));

        // When
        var result = entityDevicesService
                .update(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertEquals("UPDATED DEVICE", result.getName()); // EntityDevice converts to uppercase
        verify(entityDeviceRepository).update(testDevice);
    }

    @Test
    @DisplayName("Should update multiple devices successfully")
    void testUpdate_MultipleDevices() {
        // Given
        EntityDevice device2 = new EntityDevice();
        device2.setId(UUID.randomUUID());
        device2.setName("Updated Device 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        List<EntityDevice> devices = Arrays.asList(testDevice, device2);
        when(entityDeviceRepository.update(devices)).thenReturn(Uni.createFrom().item(devices));

        // When
        var result = entityDevicesService
                .update(devices)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertEquals(2, result.size());
        verify(entityDeviceRepository).update(devices);
    }

    @Test
    @DisplayName("Should delete device successfully")
    void testDelete_Success() {
        // Given
        when(entityDeviceRepository.findById(testId))
                .thenReturn(Uni.createFrom().item(Optional.of(testDevice)));
        when(entityDeviceRepository.delete(testDevice))
                .thenReturn(Uni.createFrom().voidItem());

        // When
        entityDevicesService
                .delete(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();

        // Then
        verify(entityDeviceRepository).findById(testId);
        verify(entityDeviceRepository).delete(testDevice);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting non-existent device")
    void testDelete_NotFound() {
        // Given
        when(entityDeviceRepository.findById(testId))
                .thenReturn(Uni.createFrom().item(Optional.empty()));

        // When & Then
        entityDevicesService
                .delete(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(EntityNotFoundException.class);

        verify(entityDeviceRepository).findById(testId);
        verify(entityDeviceRepository, never()).delete(any());
    }
}
