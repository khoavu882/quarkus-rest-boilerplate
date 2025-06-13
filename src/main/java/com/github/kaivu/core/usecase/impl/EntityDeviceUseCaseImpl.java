package com.github.kaivu.core.usecase.impl;

import com.github.kaivu.core.dto.CreateEntityDTO;
import com.github.kaivu.core.dto.UpdateEntityDTO;
import com.github.kaivu.core.entities.EntityDevice;
import com.github.kaivu.core.usecase.EntityDeviceUseCase;
import com.github.kaivu.domain.mappers.EntityDeviceMapper;
import com.github.kaivu.domain.models.EntityDeviceDetailsVM;
import com.github.kaivu.domain.models.EntityDeviceFilters;
import com.github.kaivu.domain.models.EntityDeviceVM;
import com.github.kaivu.domain.repositories.ext.EntityDeviceRepository;
import com.github.kaivu.domain.services.EntityDevicesService;
import com.github.kaivu.infrastructure.common.PageResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/13/25
 * Time: 11:49 AM
 */
@Slf4j
@ApplicationScoped
public class EntityDeviceUseCaseImpl implements EntityDeviceUseCase {

    @Inject
    EntityDeviceRepository entityDeviceRepository;

    @Inject
    EntityDevicesService entityDevicesService;

    @Override
    @Transactional
    public Uni<EntityDeviceVM> create(CreateEntityDTO dto) {
        EntityDevice entityDevice = EntityDeviceMapper.map.toEntity(dto);
        return entityDevicesService.persist(entityDevice).map(EntityDeviceMapper.map::toEntityDeviceVM);
    }

    @Override
    public Uni<EntityDeviceVM> update(UUID id, UpdateEntityDTO dto) {
        return entityDevicesService
                .getById(id)
                .flatMap(entity -> {
                    entity.setName(dto.name());
                    entity.setDescription(dto.description());
                    return entityDevicesService.update(entity);
                })
                .map(EntityDeviceMapper.map::toEntityDeviceVM);
    }

    @Override
    public Uni<EntityDeviceDetailsVM> details(UUID id) {
        return entityDevicesService.getById(id).map(EntityDeviceMapper.map::toEntityDeviceDetailVM);
    }

    @Override
    public Uni<PageResponse<EntityDeviceVM>> pageable(EntityDeviceFilters filters) {
        return entityDeviceRepository.findAll(filters).map(data -> PageResponse.<EntityDeviceVM>builder()
                .content(data.getItem1().stream()
                        .map(EntityDeviceMapper.map::toEntityDeviceVM)
                        .toList())
                .totalElements(data.getItem2().intValue())
                .page(filters.getPage())
                .size(filters.getSize())
                .build());
    }

    @Override
    public Uni<Void> delete(UUID id) {
        return entityDevicesService.delete(id);
    }
}
