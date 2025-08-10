package com.github.kaivu.application.usecase.impl;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.application.port.IEntityDeviceRepository;
import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.application.usecase.EntityDeviceUseCase;
import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.AsyncObservabilityContext;
import com.github.kaivu.common.context.LanguageContext;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.context.TenantObservabilityContext;
import com.github.kaivu.common.exception.ObservableServiceException;
import com.github.kaivu.common.mapper.EntityDeviceMapper;
import com.github.kaivu.common.service.ObservabilityService;
import com.github.kaivu.config.ApplicationConfiguration;
import com.github.kaivu.config.annotations.LogExecutionTime;
import com.github.kaivu.config.annotations.Observability;
import com.github.kaivu.config.handler.ErrorsEnum;
import com.github.kaivu.domain.EntityDevice;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/13/25
 * Time: 11:49 AM
 */
@Slf4j
@ApplicationScoped
@Observability(layer = ObservabilityConstant.LAYER_USECASE)
public class EntityDeviceUseCaseImpl implements EntityDeviceUseCase {

    private final ApplicationConfiguration config;
    private final IEntityDeviceRepository entityDeviceRepository;
    private final CacheService cacheService;
    private final ObservabilityService observabilityService;
    private final ObservabilityContext observabilityContext;
    private final TenantObservabilityContext tenantContext;
    private final AsyncObservabilityContext asyncContext;
    private final LanguageContext languageContext;

    @Inject
    public EntityDeviceUseCaseImpl(
            ApplicationConfiguration config,
            IEntityDeviceRepository entityDeviceRepository,
            CacheService cacheService,
            ObservabilityService observabilityService,
            ObservabilityContext observabilityContext,
            TenantObservabilityContext tenantContext,
            AsyncObservabilityContext asyncContext,
            LanguageContext languageContext) {
        this.config = config;
        this.entityDeviceRepository = entityDeviceRepository;
        this.cacheService = cacheService;
        this.observabilityService = observabilityService;
        this.observabilityContext = observabilityContext;
        this.tenantContext = tenantContext;
        this.asyncContext = asyncContext;
        this.languageContext = languageContext;
    }

    @Override
    @Observability("create_entity_device")
    @LogExecutionTime
    @WithTransaction
    public Uni<EntityDeviceVM> create(CreateEntityDTO dto) {
        return observabilityService
                .operation("create_entity_device")
                .layer(ObservabilityConstant.LAYER_USECASE)
                .executeUni(validateNameUniqueness(dto.name())
                        .flatMap(ignored -> createAndCache(dto))
                        .map(EntityDeviceMapper.map::toEntityDeviceVM)
                        .call(ignored -> invalidatePageCacheAsync())
                        .invoke(vm -> log.info(
                                "{}UseCase: Created entity device [id={}, context={}]",
                                tenantContext.getTenantLogPrefix(),
                                vm.getId(),
                                observabilityContext.getContextSummary())));
    }

    @Override
    @WithTransaction
    public Uni<EntityDeviceVM> update(UUID id, UpdateEntityDTO dto) {
        return getById(id)
                .flatMap(entity -> validateNameForUpdate(entity, dto.name()))
                .flatMap(entity -> updateEntityFields(entity, dto))
                .flatMap(this::updateCacheAfterModification)
                .map(EntityDeviceMapper.map::toEntityDeviceVM)
                .call(ignored -> invalidatePageCacheAsync())
                .invoke(vm -> log.debug("UseCase: Updated entity with ID: {}", id));
    }

    @Override
    public Uni<EntityDeviceDetailsVM> details(UUID id) {
        String cacheKey = cacheService.generateKey(config.cache.prefix.entityDeviceDetails, id.toString());

        return cacheService
                .getOrCompute(
                        cacheKey,
                        EntityDeviceDetailsVM.class,
                        () -> getByIdWithCaching(id)
                                .map(EntityDeviceMapper.map::toEntityDeviceDetailVM)
                                .invoke(() ->
                                        log.debug("UseCase: Loaded entity details from repository for ID: {}", id)),
                        Duration.ofMillis(config.cache.entityDeviceDetails.ttlMs))
                .invoke(() -> log.debug("UseCase: Retrieved entity details for ID: {} from cache", id));
    }

    @Override
    public Uni<PageResponse<EntityDeviceVM>> pageable(EntityDeviceFilters filters) {
        // Generate cache key based on filters
        String cacheKey = generatePageCacheKey(filters);

        return cacheService.get(cacheKey, PageResponse.class).flatMap(cachedResult -> {
            if (cachedResult.isPresent()) {
                log.debug("UseCase: Retrieved page data from cache with key: {}", cacheKey);
                @SuppressWarnings("unchecked")
                PageResponse<EntityDeviceVM> typedResult = cachedResult.get();
                return Uni.createFrom().item(typedResult);
            } else {
                return Uni.combine()
                        .all()
                        .unis(findWithFilters(filters), countWithFilters(filters))
                        .with((data, total) -> PageResponse.<EntityDeviceVM>builder()
                                .content(data.stream()
                                        .map(EntityDeviceMapper.map::toEntityDeviceVM)
                                        .toList())
                                .totalElements(total.intValue())
                                .page(filters.getPage())
                                .size(filters.getSize())
                                .build())
                        .flatMap(pageResponse -> cacheService
                                .set(cacheKey, pageResponse, Duration.ofMillis(config.cache.entityDevicePage.ttlMs))
                                .replaceWith(pageResponse))
                        .invoke(() -> log.debug("UseCase: Loaded page data from service for filters: {}", filters));
            }
        });
    }

    @Override
    @WithTransaction
    public Uni<Void> delete(UUID id) {
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
                .invoke(() -> {
                    log.debug("UseCase: Deleted entity and cleared caches: {}", id);
                    invalidatePageCacheAsync();
                });
    }

    /**
     * Generate cache key for paginated results based on filters
     */
    private String generatePageCacheKey(EntityDeviceFilters filters) {
        return cacheService.generateKey(
                config.cache.prefix.entityDevicePage,
                String.valueOf(filters.getPage()),
                String.valueOf(filters.getSize()),
                filters.getName() != null ? filters.getName() : "null",
                filters.getStatus() != null ? filters.getStatus().name() : "null");
    }

    /**
     * Async invalidate all page cache entries when entities are modified
     */
    private Uni<Void> invalidatePageCacheAsync() {
        String pattern = tenantContext.getTenantCacheKeyPrefix(
                cacheService.generateKey(config.cache.prefix.entityDevicePage, "*"));

        return cacheService
                .deleteByPattern(pattern)
                .invoke(count -> log.debug(
                        "{}Invalidated {} page cache entries [context={}]",
                        tenantContext.getTenantLogPrefix(),
                        count,
                        observabilityContext.getContextSummary()))
                .onFailure()
                .invoke(failure -> log.warn(
                        "{}Failed to invalidate page cache [context={}, error={}]",
                        tenantContext.getTenantLogPrefix(),
                        observabilityContext.getContextSummary(),
                        failure.getMessage(),
                        failure))
                .replaceWithVoid();
    }

    // Private helper methods for business logic

    private Uni<Optional<EntityDevice>> findById(UUID id) {
        return entityDeviceRepository.findById(id);
    }

    private Uni<EntityDevice> getById(UUID id) throws EntityNotFoundException {
        return findById(id)
                .map(entityOpt -> entityOpt.orElseThrow(() -> new EntityNotFoundException(
                        ErrorsEnum.ENTITY_DEVICE_NOT_FOUND.withLocale(languageContext.getCurrentLocale(), id))));
    }

    private Uni<Optional<EntityDevice>> findByName(String name) {
        return entityDeviceRepository.findByName(name);
    }

    private Uni<Void> validateNameUniqueness(String name) {
        return findByName(name).flatMap(existingEntity -> {
            if (existingEntity.isPresent()) {
                return Uni.createFrom()
                        .failure(new ObservableServiceException(
                                ErrorsEnum.ENTITY_DEVICE_NAME_ALREADY_EXISTS.withLocale(
                                        languageContext.getCurrentLocale(), name),
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

    private Uni<EntityDevice> getByIdWithCaching(UUID id) {
        String cacheKey = cacheService.generateKey(config.cache.prefix.entityDevice, id.toString());

        return cacheService
                .getOrCompute(
                        cacheKey,
                        EntityDevice.class,
                        () -> getById(id),
                        Duration.ofMillis(config.cache.entityDevice.ttlMs))
                .invoke(entity -> log.debug("Retrieved entity with ID: {} from cache", id));
    }

    private Uni<List<EntityDevice>> findWithFilters(EntityDeviceFilters filters) {
        return entityDeviceRepository
                .findAll(filters)
                .invoke(entities -> log.debug("Found {} entities with filters", entities.size()));
    }

    private Uni<Long> countWithFilters(EntityDeviceFilters filters) {
        return entityDeviceRepository
                .countAll(filters)
                .invoke(count -> log.debug("Counted {} entities with filters", count));
    }
}
