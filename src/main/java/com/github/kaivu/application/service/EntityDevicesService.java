package com.github.kaivu.application.service;

import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link EntityDevicesService}.
 */
public interface EntityDevicesService {

    // Read operations
    Uni<Optional<EntityDevice>> findById(UUID id);

    Uni<EntityDevice> getById(UUID identify) throws EntityNotFoundException;

    Uni<Optional<EntityDevice>> findByName(String name);

    Uni<EntityDevice> getByName(String name);

    // Write operations
    Uni<EntityDevice> persist(EntityDevice entity);

    Uni<List<EntityDevice>> persist(List<EntityDevice> entities);

    Uni<EntityDevice> update(EntityDevice entity) throws EntityNotFoundException;

    Uni<List<EntityDevice>> update(List<EntityDevice> entities) throws EntityNotFoundException;

    Uni<Void> delete(UUID identify) throws EntityNotFoundException;
}
