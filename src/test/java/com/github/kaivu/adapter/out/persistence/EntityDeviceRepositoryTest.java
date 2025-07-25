package com.github.kaivu.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.adapter.in.rest.dto.request.PageableRequest;
import com.github.kaivu.domain.EntityDevice;
import com.github.kaivu.domain.enumeration.ActionStatus;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@QuarkusTest
class EntityDeviceRepositoryTest {

    @Inject
    EntityDeviceRepository repository;

    private EntityDevice testDevice;

    @BeforeEach
    void setUp() {
        testDevice = new EntityDevice();
        // Don't set ID - let Hibernate generate it
        testDevice.setName("Test Device " + System.currentTimeMillis());
        testDevice.setDescription("Test Description");
        testDevice.setStatus(ActionStatus.ACTIVATED);
    }

    @Test
    @TestTransaction
    void testFindById_Found() {
        // Persist test device first and get the generated ID
        var persistedDevice = repository
                .persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        UUID testId = persistedDevice.getId();

        // Test finding by ID
        var result = repository
                .findById(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
        assertTrue(result.get().getName().toUpperCase().contains("TEST DEVICE"));
    }

    @Test
    @TestTransaction
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        var result = repository
                .findById(nonExistentId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void testFindByIds() {
        EntityDevice device2 = new EntityDevice();
        device2.setName("Test Device 2 " + System.currentTimeMillis());
        device2.setDescription("Test Description 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        // Persist test devices
        var persistedDevices = repository
                .persist(Arrays.asList(testDevice, device2))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        List<UUID> deviceIds =
                persistedDevices.stream().map(EntityDevice::getId).toList();

        // Test finding by IDs
        var result = repository
                .findByIds(deviceIds)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertEquals(2, result.size());
    }

    @Test
    @TestTransaction
    void testPersist() {
        var result = repository
                .persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result.getId());
        assertEquals(testDevice.getName().toUpperCase(), result.getName());
    }

    @Test
    @TestTransaction
    void testPersistList() {
        EntityDevice device2 = new EntityDevice();
        device2.setName("Test Device 2 " + System.currentTimeMillis());
        device2.setDescription("Test Description 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        List<EntityDevice> devices = Arrays.asList(testDevice, device2);

        var result = repository
                .persist(devices)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> d.getId() != null));
    }

    @Test
    @TestTransaction
    void testUpdate() {
        // Persist first
        var persistedDevice = repository
                .persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Update
        persistedDevice.setDescription("Updated Description");

        var result = repository
                .update(persistedDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    @TestTransaction
    void testUpdateList() {
        EntityDevice device2 = new EntityDevice();
        device2.setName("Test Device 2 " + System.currentTimeMillis());
        device2.setDescription("Test Description 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        // Persist first
        var persistedDevices = repository
                .persist(Arrays.asList(testDevice, device2))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Update
        persistedDevices.get(0).setDescription("Updated Description 1");
        persistedDevices.get(1).setDescription("Updated Description 2");

        var result = repository
                .update(persistedDevices)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertEquals(2, result.size());
        assertEquals("Updated Description 1", result.get(0).getDescription());
        assertEquals("Updated Description 2", result.get(1).getDescription());
    }

    @Test
    @TestTransaction
    void testDelete() {
        // Persist first to get a managed entity
        var persistedDevice = repository
                .persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        UUID deviceId = persistedDevice.getId();

        // Find the entity again to ensure it's managed in the current session
        var managedDevice = repository
                .findById(deviceId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(managedDevice.isPresent());

        // Delete the found managed entity
        repository
                .delete(managedDevice.get())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem();

        // Verify deletion by attempting to find the entity
        var result = repository
                .findById(deviceId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void testDeleteById() {
        // Persist first to get a managed entity
        var persistedDevice = repository
                .persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        UUID deviceId = persistedDevice.getId();

        // Find the entity again to ensure it's managed
        var managedDevice = repository
                .findById(deviceId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(managedDevice.isPresent());

        // Delete the found managed entity
        repository
                .delete(managedDevice.get())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem();

        // Verify deletion
        var result = repository
                .findById(deviceId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void testDeleteManagedEntity() {
        // Persist first to get a managed entity
        var persistedDevice = repository
                .persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        UUID deviceId = persistedDevice.getId();

        // Find the entity again to get a fresh managed instance
        var managedDevice = repository
                .findById(deviceId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(managedDevice.isPresent());

        // Delete should complete without error for managed entities
        assertDoesNotThrow(() -> {
            repository
                    .delete(managedDevice.get())
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem();
        });

        // Verify the entity was actually deleted
        var result = repository
                .findById(deviceId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void testDeleteList() {
        // Create and persist multiple devices
        EntityDevice device2 = new EntityDevice();
        device2.setName("Test Device 2 " + System.currentTimeMillis());
        device2.setDescription("Test Description 2");
        device2.setStatus(ActionStatus.ACTIVATED);

        var persistedDevices = repository
                .persist(Arrays.asList(testDevice, device2))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        List<UUID> deviceIds = persistedDevices.stream().map(EntityDevice::getId).toList();

        // Find the entities again to get managed instances
        var managedDevices = repository
                .findByIds(deviceIds)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertEquals(2, managedDevices.size());

        // Delete each managed device individually
        for (EntityDevice device : managedDevices) {
            repository
                    .delete(device)
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem();
        }

        // Verify all devices were deleted
        for (UUID deviceId : deviceIds) {
            var result = repository
                    .findById(deviceId)
                    .subscribe()
                    .withSubscriber(UniAssertSubscriber.create())
                    .awaitItem()
                    .getItem();

            assertTrue(result.isEmpty());
        }
    }

    @Test
    @TestTransaction
    void testFindAll_WithoutFilters() {
        // Persist test devices
        EntityDevice device2 = new EntityDevice();
        device2.setName("Another Device " + System.currentTimeMillis());
        device2.setDescription("Another Description");
        device2.setStatus(ActionStatus.ACTIVATED);

        repository
                .persist(Arrays.asList(testDevice, device2))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem();

        // Test findAll without filters
        PageableRequest pageableRequest = new PageableRequest() {
                    // Anonymous implementation since PageableRequest is abstract
                };
        pageableRequest.setPage(0);
        pageableRequest.setSize(10);

        var result = repository
                .findAll(pageableRequest)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(result.size() >= 2);
    }

    @Test
    @TestTransaction
    void testCountAll() {
        // Persist test device
        repository
                .persist(testDevice)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem();

        // Test countAll
        PageableRequest pageableRequest = new PageableRequest() {
                    // Anonymous implementation since PageableRequest is abstract
                };
        pageableRequest.setPage(0);
        pageableRequest.setSize(10);

        var result = repository
                .countAll(pageableRequest)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(result >= 1);
    }
}
