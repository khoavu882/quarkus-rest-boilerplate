package com.github.kaivu.application.usecase;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case interface for EntityDevice operations
 */
public interface EntityDeviceUseCase {

    // Basic CRUD operations
    Uni<Optional<EntityDevice>> findById(UUID id);

    Uni<EntityDevice> getById(UUID id);

    Uni<EntityDevice> persist(EntityDevice entity);

    Uni<List<EntityDevice>> persist(List<EntityDevice> entities);

    Uni<EntityDevice> update(EntityDevice entity);

    Uni<List<EntityDevice>> update(List<EntityDevice> entities);

    Uni<Void> delete(UUID id);

    Uni<Void> delete(List<UUID> ids);

    // REST layer methods
    Uni<EntityDeviceVM> create(CreateEntityDTO dto);

    Uni<EntityDeviceVM> update(UUID id, UpdateEntityDTO dto);

    Uni<EntityDeviceDetailsVM> details(UUID id);

    Uni<PageResponse<EntityDeviceVM>> pageable(EntityDeviceFilters filters);
}
