package com.github.kaivu.application.usecase.impl;

import com.github.kaivu.adapter.in.rest.EntityDevicesResource;
import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import com.github.kaivu.adapter.out.persistence.EntityDeviceRepository;
import com.github.kaivu.application.service.EntityDevicesService;
import com.github.kaivu.application.usecase.EntityDeviceUseCase;
import com.github.kaivu.common.mapper.EntityDeviceMapper;
import com.github.kaivu.domain.EntityDevice;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    @Inject
    EntityDevicesResource entityDevicesResource;

    @Override
    @WithTransaction
    public Uni<EntityDeviceVM> create(CreateEntityDTO dto) {
        EntityDevice entityDevice = EntityDeviceMapper.map.toEntity(dto);
        return entityDevicesService.persist(entityDevice).map(EntityDeviceMapper.map::toEntityDeviceVM);
    }

    @Override
    @WithTransaction
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
        return Uni.combine()
                .all()
                .unis(entityDeviceRepository.findAll(filters), entityDeviceRepository.countAll(filters))
                .with((data, total) -> PageResponse.<EntityDeviceVM>builder()
                        .content(data.stream()
                                .map(EntityDeviceMapper.map::toEntityDeviceVM)
                                .toList())
                        .totalElements(total.intValue())
                        .page(filters.getPage())
                        .size(filters.getSize())
                        .build());
    }

    @Override
    public Uni<Void> delete(UUID id) {
        return entityDevicesService.delete(id);
    }
}
