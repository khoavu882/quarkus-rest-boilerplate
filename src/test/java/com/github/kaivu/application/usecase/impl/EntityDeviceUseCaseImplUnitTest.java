package com.github.kaivu.application.usecase.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.kaivu.adapter.in.rest.EntityDevicesResource;
import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.out.persistence.EntityDeviceRepository;
import com.github.kaivu.application.service.EntityDevicesService;
import com.github.kaivu.domain.EntityDevice;
import com.github.kaivu.domain.enumeration.ActionStatus;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for EntityDeviceUseCaseImpl using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EntityDeviceUseCase Unit Tests")
class EntityDeviceUseCaseImplUnitTest {

    @Mock
    private EntityDeviceRepository entityDeviceRepository;

    @Mock
    private EntityDevicesService entityDevicesService;

    @Mock
    private EntityDevicesResource entityDevicesResource;

    @InjectMocks
    private EntityDeviceUseCaseImpl entityDeviceUseCase;

    private EntityDevice testDevice;
    private UUID testId;
    private CreateEntityDTO createDto;
    private UpdateEntityDTO updateDto;
    private EntityDeviceFilters filters;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testDevice = new EntityDevice();
        testDevice.setId(testId);
        testDevice.setName("Test Device");
        testDevice.setDescription("Test Description");
        testDevice.setStatus(ActionStatus.ACTIVATED);

        createDto = new CreateEntityDTO("Test Device", "Test Description");
        updateDto = new UpdateEntityDTO("Updated Device", "Updated Description");

        filters = new EntityDeviceFilters() {
            @Override
            public int getPage() {
                return 0;
            }

            @Override
            public int getSize() {
                return 10;
            }

            @Override
            public String getKeyword() {
                return null;
            }
        };
    }

    @Test
    @DisplayName("Should create entity device successfully")
    void testCreate_Success() {
        // Given
        when(entityDevicesService.persist(any(EntityDevice.class)))
                .thenReturn(Uni.createFrom().item(testDevice));

        // When
        var result = entityDeviceUseCase
                .create(createDto)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals("TEST DEVICE", result.getName()); // EntityDevice converts to uppercase
        verify(entityDevicesService).persist(any(EntityDevice.class));
    }

    @Test
    @DisplayName("Should update entity device successfully")
    void testUpdate_Success() {
        // Given
        when(entityDevicesService.getById(testId)).thenReturn(Uni.createFrom().item(testDevice));
        when(entityDevicesService.update(any(EntityDevice.class)))
                .thenReturn(Uni.createFrom().item(testDevice));

        // When
        var result = entityDeviceUseCase
                .update(testId, updateDto)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        verify(entityDevicesService).getById(testId);
        verify(entityDevicesService).update(any(EntityDevice.class));
    }

    @Test
    @DisplayName("Should get entity device details successfully")
    void testDetails_Success() {
        // Given
        when(entityDevicesService.getById(testId)).thenReturn(Uni.createFrom().item(testDevice));

        // When
        var result = entityDeviceUseCase
                .details(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals("TEST DEVICE", result.getName()); // EntityDevice converts to uppercase
        verify(entityDevicesService).getById(testId);
    }

    @Test
    @DisplayName("Should get pageable entity devices successfully")
    void testPageable_Success() {
        // Given
        List<EntityDevice> devices = Arrays.asList(testDevice);
        when(entityDeviceRepository.findAll(filters))
                .thenReturn(Uni.createFrom().item(devices));
        when(entityDeviceRepository.countAll(filters))
                .thenReturn(Uni.createFrom().item(1L));

        // When
        var result = entityDeviceUseCase
                .pageable(filters)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("TEST DEVICE", result.getContent().getFirst().getName()); // EntityDevice converts to uppercase
        verify(entityDeviceRepository).findAll(filters);
        verify(entityDeviceRepository).countAll(filters);
    }

    @Test
    @DisplayName("Should delete entity device successfully")
    void testDelete_Success() {
        // Given
        when(entityDevicesService.delete(testId)).thenReturn(Uni.createFrom().voidItem());

        // When
        entityDeviceUseCase
                .delete(testId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();

        // Then
        verify(entityDevicesService).delete(testId);
    }

    @Test
    @DisplayName("Should handle empty pageable results")
    void testPageable_EmptyResults() {
        // Given
        List<EntityDevice> devices = List.of();
        when(entityDeviceRepository.findAll(filters))
                .thenReturn(Uni.createFrom().item(devices));
        when(entityDeviceRepository.countAll(filters))
                .thenReturn(Uni.createFrom().item(0L));

        // When
        var result = entityDeviceUseCase
                .pageable(filters)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        verify(entityDeviceRepository).findAll(filters);
        verify(entityDeviceRepository).countAll(filters);
    }
}
