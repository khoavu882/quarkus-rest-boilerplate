package com.github.kaivu.application.service;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.common.service.BaseReadService;
import com.github.kaivu.common.service.BaseWriteService;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link EntityDevicesService}.
 */
public interface EntityDevicesService
        extends BaseReadService<EntityDevice, UUID>, BaseWriteService<EntityDevice, UUID> {

    Uni<Optional<EntityDevice>> findByName(String name);

    /**
     * Check exist an Entity Device by Name.
     *
     * @param name of object.
     */
    Uni<EntityDevice> getByName(String name);

    // Business logic methods

    /**
     * Create entity with validation and caching
     */
    Uni<EntityDevice> createWithValidation(CreateEntityDTO dto);

    /**
     * Update entity with validation and cache management
     */
    Uni<EntityDevice> updateWithValidation(UUID id, UpdateEntityDTO dto);

    /**
     * Delete entity with cache cleanup
     */
    Uni<Void> deleteWithCacheCleanup(UUID id);

    /**
     * Get entity by ID with caching
     */
    Uni<EntityDevice> getByIdWithCaching(UUID id);

    /**
     * Find entities with filters and caching
     */
    Uni<List<EntityDevice>> findWithFilters(EntityDeviceFilters filters);

    /**
     * Count entities with filters
     */
    Uni<Long> countWithFilters(EntityDeviceFilters filters);
}
