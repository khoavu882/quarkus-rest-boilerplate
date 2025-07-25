package com.github.kaivu.application.service;

import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for EntityDevice operations
 */
public interface EntityDevicesService {

    Uni<Optional<EntityDevice>> findById(UUID id);

    Uni<EntityDevice> getById(UUID id);

    Uni<EntityDevice> persist(EntityDevice entity);

    Uni<List<EntityDevice>> persist(List<EntityDevice> entities);

    Uni<EntityDevice> update(EntityDevice entity);

    Uni<List<EntityDevice>> update(List<EntityDevice> entities);

    Uni<Void> delete(UUID id);

    Uni<Void> delete(List<UUID> ids);
}
