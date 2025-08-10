package com.github.kaivu.application.service.impl;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.application.port.IEntityDeviceRepository;
import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.application.service.EntityDevicesService;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.exception.ObservableServiceException;
import com.github.kaivu.common.mapper.EntityDeviceMapper;
import com.github.kaivu.config.ApplicationConfiguration;
import com.github.kaivu.config.handler.ErrorsEnum;
import com.github.kaivu.domain.EntityDevice;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
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

    private final ApplicationConfiguration config;
    private final IEntityDeviceRepository entityDeviceRepository;
    private final CacheService cacheService;
    private final ObservabilityContext observabilityContext;

    @Context
    ContainerRequestContext requestContext;

    @Inject
    public EntityDevicesServiceImpl(
            ApplicationConfiguration config,
            IEntityDeviceRepository entityDeviceRepository,
            CacheService cacheService,
            ObservabilityContext observabilityContext) {
        this.config = config;
        this.entityDeviceRepository = entityDeviceRepository;
        this.cacheService = cacheService;
        this.observabilityContext = observabilityContext;
    }

    @Override
    public Uni<Optional<EntityDevice>> findById(UUID id) {
        return entityDeviceRepository.findById(id);
    }

    @Override
    public Uni<Optional<EntityDevice>> findByName(String name) {
        return entityDeviceRepository.findByName(name);
    }

    @Override
    public Uni<EntityDevice> getById(UUID identify) throws EntityNotFoundException {
        return findById(identify)
                .map(entityOpt -> entityOpt.orElseThrow(() -> new EntityNotFoundException(
                        ErrorsEnum.ENTITY_DEVICE_NOT_FOUND.withLocale(requestContext.getLanguage(), identify))));
    }

    @Override
    public Uni<EntityDevice> getByName(String name) throws EntityNotFoundException {
        return findByName(name)
                .map(entityOpt -> entityOpt.orElseThrow(() -> new EntityNotFoundException(
                        ErrorsEnum.ENTITY_DEVICE_NOT_FOUND.withLocale(requestContext.getLanguage(), name))));
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

    /**
     * Business logic: Create entity with validation and caching
     */
    @WithTransaction
    public Uni<EntityDevice> createWithValidation(CreateEntityDTO dto) {
        return validateNameUniqueness(dto.name())
                .flatMap(ignored -> createAndCache(dto))
                .invoke(entity -> log.debug("Created entity with ID: {} and cached", entity.getId()));
    }

    /**
     * Business logic: Update entity with validation and cache management
     */
    @WithTransaction
    public Uni<EntityDevice> updateWithValidation(UUID id, UpdateEntityDTO dto) {
        return getById(id)
                .flatMap(entity -> validateNameForUpdate(entity, dto.name()))
                .flatMap(entity -> updateEntityFields(entity, dto))
                .flatMap(this::updateCacheAfterModification)
                .invoke(entity -> log.debug("Updated entity with ID: {} and refreshed cache", entity.getId()));
    }

    /**
     * Business logic: Delete entity with cache cleanup
     */
    @WithTransaction
    public Uni<Void> deleteWithCacheCleanup(UUID id) {
        return getById(id)
                .flatMap(entity -> {
                    // Remove from cache before deletion
                    String cacheKey = cacheService.generateKey(config.cache.prefix.entityDevice, id.toString());
                    String detailsCacheKey =
                            cacheService.generateKey(config.cache.prefix.entityDeviceDetails, id.toString());

                    return Uni.combine()
                            .all()
                            .unis(
                                    cacheService.delete(cacheKey),
                                    cacheService.delete(detailsCacheKey),
                                    entityDeviceRepository.delete(entity))
                            .discardItems();
                })
                .invoke(() -> log.debug("Deleted entity with ID: {} and cleared cache", id));
    }

    /**
     * Business logic: Get entity by ID with caching
     */
    public Uni<EntityDevice> getByIdWithCaching(UUID id) {
        String cacheKey = cacheService.generateKey(config.cache.prefix.entityDevice, id.toString());

        return cacheService
                .getOrCompute(
                        cacheKey,
                        EntityDevice.class,
                        () -> getById(id),
                        Duration.ofMillis(config.cache.entityDevice.ttlMs))
                .invoke(entity -> log.debug("Retrieved entity with ID: {} from cache", id));
    }

    /**
     * Business logic: Find entities with filters and caching
     */
    public Uni<List<EntityDevice>> findWithFilters(EntityDeviceFilters filters) {
        return entityDeviceRepository
                .findAll(filters)
                .invoke(entities -> log.debug("Found {} entities with filters", entities.size()));
    }

    /**
     * Business logic: Count entities with filters
     */
    public Uni<Long> countWithFilters(EntityDeviceFilters filters) {
        return entityDeviceRepository
                .countAll(filters)
                .invoke(count -> log.debug("Counted {} entities with filters", count));
    }

    // Private helper methods for business logic

    private Uni<Void> validateNameUniqueness(String name) {
        return findByName(name).flatMap(existingEntity -> {
            if (existingEntity.isPresent()) {
                return Uni.createFrom()
                        .failure(new ObservableServiceException(
                                ErrorsEnum.ENTITY_DEVICE_NAME_ALREADY_EXISTS.withLocale(
                                        requestContext.getLanguage(), name),
                                observabilityContext));
            }
            return Uni.createFrom().voidItem();
        });
    }

    private Uni<EntityDevice> validateNameForUpdate(EntityDevice entity, String newName) {
        if (!entity.getName().equalsIgnoreCase(newName)) {
            return validateNameUniqueness(newName).replaceWith(entity);
        }
        return Uni.createFrom().item(entity);
    }

    private Uni<EntityDevice> createAndCache(CreateEntityDTO dto) {
        EntityDevice entity = EntityDeviceMapper.map.toEntity(dto);
        return entityDeviceRepository.persist(entity).flatMap(this::cacheEntity);
    }

    private Uni<EntityDevice> updateEntityFields(EntityDevice entity, UpdateEntityDTO dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        return entityDeviceRepository.update(entity);
    }

    private Uni<EntityDevice> cacheEntity(EntityDevice entity) {
        String cacheKey = cacheService.generateKey(
                config.cache.prefix.entityDevice, entity.getId().toString());
        return cacheService
                .set(cacheKey, entity, Duration.ofMillis(config.cache.entityDevice.ttlMs))
                .replaceWith(entity);
    }

    private Uni<EntityDevice> updateCacheAfterModification(EntityDevice entity) {
        String cacheKey = cacheService.generateKey(
                config.cache.prefix.entityDevice, entity.getId().toString());
        String detailsCacheKey = cacheService.generateKey(
                config.cache.prefix.entityDeviceDetails, entity.getId().toString());

        return Uni.combine()
                .all()
                .unis(
                        cacheService.set(cacheKey, entity, Duration.ofMillis(config.cache.entityDevice.ttlMs)),
                        cacheService.set(
                                detailsCacheKey, entity, Duration.ofMillis(config.cache.entityDeviceDetails.ttlMs)))
                .discardItems()
                .replaceWith(entity);
    }
}
