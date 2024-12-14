package vn.fpt.services.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import vn.fpt.models.EntityDevice;
import vn.fpt.repositories.ext.EntityDeviceRepository;
import vn.fpt.services.EntityDevicesService;
import vn.fpt.services.dto.CreateEntityDTO;
import vn.fpt.services.mappers.EntityDeviceMapper;
import vn.fpt.web.errors.ErrorsEnum;
import vn.fpt.web.errors.exceptions.EntityNotFoundException;
import vn.fpt.web.vm.EntityDeviceVM;

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
    @Transactional
    public Uni<EntityDeviceVM> create(CreateEntityDTO dto) {
        EntityDevice entityDevice = EntityDeviceMapper.map.toEntity(dto);
        return persist(entityDevice).map(EntityDeviceMapper.map::toEntityDeviceVM);
    }

    @Override
    public Uni<Optional<EntityDevice>> findById(UUID id) {
        return entityDeviceRepository.findById(id);
    }

    @Override
    public Uni<EntityDevice> getById(UUID identify) throws EntityNotFoundException {
        return findById(identify)
                .map(entityOpt -> entityOpt.orElseThrow(() -> new EntityNotFoundException(
                        ErrorsEnum.TICKET_NOT_FOUND.withLocale(requestContext.getLanguage()))));
    }

    @Override
    @Transactional
    public Uni<EntityDevice> persist(EntityDevice entity) {
        return entityDeviceRepository.persist(entity);
    }

    @Override
    @Transactional
    public Uni<List<EntityDevice>> persist(List<EntityDevice> entities) {
        return entityDeviceRepository.persist(entities);
    }

    @Override
    @Transactional
    public Uni<EntityDevice> update(EntityDevice entity) throws EntityNotFoundException {
        return entityDeviceRepository.update(entity);
    }

    @Override
    @Transactional
    public Uni<List<EntityDevice>> update(List<EntityDevice> entities) throws EntityNotFoundException {
        return entityDeviceRepository.update(entities);
    }

    @Override
    @Transactional
    public Uni<Void> delete(UUID identify) throws EntityNotFoundException {
        return getById(identify).flatMap(entity -> entityDeviceRepository.delete(entity));
    }
}
