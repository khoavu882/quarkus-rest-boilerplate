package com.github.kaivu.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.github.kaivu.adapter.out.persistence.EntityDeviceRepository;
import com.github.kaivu.application.exception.EntityNotFoundException;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@QuarkusTest
class EntityDevicesServiceImplTest {

    @Inject
    EntityDevicesServiceImpl service;

    @InjectMock
    EntityDeviceRepository repository;

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
        when(repository.findById(testId)).thenReturn(Uni.createFrom().item(Optional.of(testDevice)));

        // When
        var result = service.findById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
    }

    @Test
    @TestTransaction
    void testFindById_NotFound() {
        // Given
        when(repository.findById(testId)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        var result = service.findById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void testGetById_Found() {
        // Given
        when(repository.findById(testId)).thenReturn(Uni.createFrom().item(Optional.of(testDevice)));

        // When
        var result = service.getById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertEquals(testId, result.getId());
        assertEquals("TEST DEVICE", result.getName());
    }

    @Test
    @TestTransaction
    void testGetById_NotFound() {
        // Given
        when(repository.findById(testId)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When & Then
        service.getById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(EntityNotFoundException.class);
    }

    @Test
    @TestTransaction
    void testPersist() {
        // Given
        when(repository.persist(any(EntityDevice.class)))
                .thenReturn(Uni.createFrom().item(testDevice));

        // When
        var result = service.persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("Test Device", result.getName());
    }

    @Test
    @TestTransaction
    void testPersistList() {
        // Given
        EntityDevice device2 = new EntityDevice();
        UUID device2Id = UUID.randomUUID();
        device2.setId(device2Id);
        device2.setName("Test Device 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        List<EntityDevice> devices = Arrays.asList(testDevice, device2);
        when(repository.persist(anyList())).thenReturn(Uni.createFrom().item(devices));

        // When
        var result = service.persist(devices)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @TestTransaction
    void testUpdate() {
        // Given
        when(repository.update(any(EntityDevice.class)))
                .thenReturn(Uni.createFrom().item(testDevice));

        // When
        var result = service.update(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(testId, result.getId());
    }

    @Test
    @TestTransaction
    void testUpdateList() {
        // Given
        EntityDevice device2 = new EntityDevice();
        UUID device2Id = UUID.randomUUID();
        device2.setId(device2Id);
        device2.setName("Test Device 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        List<EntityDevice> devices = Arrays.asList(testDevice, device2);
        when(repository.update(anyList())).thenReturn(Uni.createFrom().item(devices));

        // When
        var result = service.update(devices)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @TestTransaction
    void testDelete_Success() {
        // Given
        when(repository.findById(testId)).thenReturn(Uni.createFrom().item(Optional.of(testDevice)));
        when(repository.delete(any(EntityDevice.class)))
                .thenReturn(Uni.createFrom().voidItem());

        // When & Then
        assertDoesNotThrow(() -> {
            service.delete(testId)
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem();
        });
    }

    @Test
    @TestTransaction
    void testDelete_EntityNotFound() {
        // Given
        when(repository.findById(testId)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When & Then
        service.delete(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(EntityNotFoundException.class);
    }

    @Test
    @TestTransaction
    void testDelete_RepositoryFailure() {
        // Given
        when(repository.findById(testId)).thenReturn(Uni.createFrom().item(Optional.of(testDevice)));
        when(repository.delete(any(EntityDevice.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When & Then
        service.delete(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(RuntimeException.class);
    }

    @Test
    @TestTransaction
    void testDeleteMultiple_Success() {
        // Given
        EntityDevice device2 = new EntityDevice();
        UUID device2Id = UUID.randomUUID();
        device2.setId(device2Id);
        device2.setName("Test Device 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        List<UUID> ids = Arrays.asList(testId, device2Id);
        List<EntityDevice> devices = Arrays.asList(testDevice, device2);

        when(repository.findByIds(ids)).thenReturn(Uni.createFrom().item(devices));
        when(repository.delete(any(EntityDevice.class)))
                .thenReturn(Uni.createFrom().voidItem());

        // When & Then
        assertDoesNotThrow(() -> {
            // Delete each device individually
            for (UUID id : ids) {
                when(repository.findById(id)).thenReturn(Uni.createFrom().item(
                    devices.stream().filter(d -> d.getId().equals(id)).findFirst()
                ));
                service.delete(id)
                        .subscribe()
                        .withSubscriber(UniAssertSubscriber.create())
                        .awaitItem();
            }
        });
    }
}
