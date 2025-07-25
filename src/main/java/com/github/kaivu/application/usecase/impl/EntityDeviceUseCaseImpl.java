package com.github.kaivu.application.usecase.impl;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import com.github.kaivu.application.service.EntityDevicesService;
import com.github.kaivu.application.usecase.EntityDeviceUseCase;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of EntityDeviceUseCase
 */
@Slf4j
@ApplicationScoped
public class EntityDeviceUseCaseImpl implements EntityDeviceUseCase {

    @Inject
    EntityDevicesService entityDevicesService;

    // Basic CRUD operations (delegate to service)
    @Override
    public Uni<Optional<EntityDevice>> findById(UUID id) {
        return entityDevicesService.findById(id);
    }

    @Override
    public Uni<EntityDevice> getById(UUID id) {
        return entityDevicesService.getById(id);
    }

    @Override
    public Uni<EntityDevice> persist(EntityDevice entity) {
        return entityDevicesService.persist(entity);
    }

    @Override
    public Uni<List<EntityDevice>> persist(List<EntityDevice> entities) {
        return entityDevicesService.persist(entities);
    }

    @Override
    public Uni<EntityDevice> update(EntityDevice entity) {
        return entityDevicesService.update(entity);
    }

    @Override
    public Uni<List<EntityDevice>> update(List<EntityDevice> entities) {
        return entityDevicesService.update(entities);
    }

    @Override
    public Uni<Void> delete(UUID id) {
        return entityDevicesService.delete(id);
    }

    @Override
    public Uni<Void> delete(List<UUID> ids) {
        return entityDevicesService.delete(ids);
    }

    // REST layer methods (stub implementations to fix compilation)
    @Override
    public Uni<EntityDeviceVM> create(CreateEntityDTO dto) {
        // TODO: Implement with proper mapping
        EntityDevice entity = new EntityDevice();
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        return persist(entity).map(this::mapToVM);
    }

    @Override
    public Uni<EntityDeviceVM> update(UUID id, UpdateEntityDTO dto) {
        // TODO: Implement with proper mapping
        return getById(id)
                .map(entity -> {
                    entity.setName(dto.name());
                    entity.setDescription(dto.description());
                    return entity;
                })
                .chain(this::update)
                .map(this::mapToVM);
    }

    @Override
    public Uni<EntityDeviceDetailsVM> details(UUID id) {
        // TODO: Implement with proper mapping
        return getById(id).map(this::mapToDetailsVM);
    }

    @Override
    public Uni<PageResponse<EntityDeviceVM>> pageable(EntityDeviceFilters filters) {
        // TODO: Implement with proper pagination
        return Uni.createFrom().item(new PageResponse<>(
                List.of(),  // content
                0,          // totalElements
                0,          // page
                10          // size
        ));
    }

    // Helper mapping methods (simplified for compilation)
    private EntityDeviceVM mapToVM(EntityDevice entity) {
        return new EntityDeviceVM(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus()
        );
    }

    private EntityDeviceDetailsVM mapToDetailsVM(EntityDevice entity) {
        EntityDeviceDetailsVM vm = new EntityDeviceDetailsVM();
        vm.setId(entity.getId());
        vm.setName(entity.getName());
        vm.setDescription(entity.getDescription());
        vm.setStatus(entity.getStatus());
        return vm;
    }
}
