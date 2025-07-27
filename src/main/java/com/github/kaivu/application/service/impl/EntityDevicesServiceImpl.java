package com.github.kaivu.application.service.impl;

import com.github.kaivu.adapter.out.persistence.EntityDeviceRepository;
import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.application.service.EntityDevicesService;
import com.github.kaivu.config.handler.ErrorsEnum;
import com.github.kaivu.domain.EntityDevice;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/10/24
 * Time: 2:37 PM
 */
@Slf4j
@ApplicationScoped
public class EntityDevicesServiceImpl implements EntityDevicesService {

    @Context
    ContainerRequestContext requestContext;

    @Inject
    EntityDeviceRepository entityDeviceRepository;

    @Override
    public Uni<Optional<EntityDevice>> findById(UUID id) {
        return entityDeviceRepository.findById(id);
    }

    @Override
    public Uni<EntityDevice> getById(UUID identify) throws EntityNotFoundException {
        return findById(identify)
                .map(entityOpt -> entityOpt.orElseThrow(() -> new EntityNotFoundException(
                        ErrorsEnum.ENTITY_DEVICE_NOT_FOUND.withLocale(requestContext.getLanguage(), identify))));
    }

    @Override
    @WithTransaction
    public Uni<EntityDevice> persist(EntityDevice entity) {
        return entityDeviceRepository.persist(entity);
    }

    @Override
    @WithTransaction
    public Uni<List<EntityDevice>> persist(List<EntityDevice> entities) {
        return entityDeviceRepository.persist(entities);
    }

    @Override
    @WithTransaction
    public Uni<EntityDevice> update(EntityDevice entity) throws EntityNotFoundException {
        return entityDeviceRepository.update(entity);
    }

    @Override
    @WithTransaction
    public Uni<List<EntityDevice>> update(List<EntityDevice> entities) throws EntityNotFoundException {
        return entityDeviceRepository.update(entities);
    }

    @Override
    @WithTransaction
    public Uni<Void> delete(UUID identify) throws EntityNotFoundException {
        return getById(identify).flatMap(entity -> entityDeviceRepository.delete(entity));
    }
}
