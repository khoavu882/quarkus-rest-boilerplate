package com.github.kaivu.application.usecase.impl;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import com.github.kaivu.adapter.out.persistence.EntityDeviceRepository;
import com.github.kaivu.application.exception.EntityConflictException;
import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.application.service.EntityDevicesService;
import com.github.kaivu.application.usecase.EntityDeviceUseCase;
import com.github.kaivu.common.mapper.EntityDeviceMapper;
import com.github.kaivu.configuration.handler.ErrorsEnum;
import com.github.kaivu.domain.EntityDevice;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
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

    private static final String CACHE_PREFIX_ENTITY = "entity_device";
    private static final String CACHE_PREFIX_DETAILS = "entity_device_details";
    private static final String CACHE_PREFIX_PAGE = "entity_device_page";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration DETAILS_CACHE_TTL = Duration.ofHours(1);

    @Context
    ContainerRequestContext requestContext;

    @Inject
    EntityDevicesService entityDevicesService;

    @Inject
    EntityDeviceRepository entityDeviceRepository;

    @Inject
    CacheService cacheService;

    @Override
    @WithTransaction
    public Uni<EntityDeviceVM> create(CreateEntityDTO dto) {
        // First check if an entity with the same name already exists
        return entityDevicesService.findByName(dto.name()).flatMap(Unchecked.function(existingEntity -> {
            if (existingEntity.isPresent()) {
                // If an entity with the same name exists, throw an exception
                throw new EntityConflictException(ErrorsEnum.ENTITY_DEVICE_NAME_ALREADY_EXISTS.withLocale(
                        requestContext.getLanguage(), dto.name()));
            }

            // Name is unique, proceed with entity creation
            EntityDevice entityDevice = EntityDeviceMapper.map.toEntity(dto);
            return entityDevicesService
                    .persist(entityDevice)
                    .flatMap(savedEntity -> {
                        // Cache the newly created entity
                        String cacheKey = cacheService.generateKey(
                                CACHE_PREFIX_ENTITY, savedEntity.getId().toString());
                        EntityDeviceVM entityVM = EntityDeviceMapper.map.toEntityDeviceVM(savedEntity);

                        return cacheService
                                .set(cacheKey, entityVM, CACHE_TTL)
                                .replaceWith(entityVM)
                                .invoke(() -> log.debug("Cached new entity with key: {}", cacheKey));
                    })
                    .invoke(this::invalidatePageCache);
        }));
    }

    @Override
    @WithTransaction
    public Uni<EntityDeviceVM> update(UUID id, UpdateEntityDTO dto) {
        String cacheKey = cacheService.generateKey(CACHE_PREFIX_ENTITY, id.toString());
        String detailsCacheKey = cacheService.generateKey(CACHE_PREFIX_DETAILS, id.toString());

        return entityDevicesService
                .getById(id)
                .flatMap(entity -> {
                    // Check if name is being changed and if new name already exists
                    if (!entity.getName().equalsIgnoreCase(dto.name())) {
                        return entityDevicesService
                                .findByName(dto.name())
                                .flatMap(Unchecked.function(existingEntity -> {
                                    if (existingEntity.isPresent()) {
                                        // If an entity with the new name exists, throw an exception
                                        throw new EntityConflictException(
                                                ErrorsEnum.ENTITY_DEVICE_NAME_ALREADY_EXISTS.withLocale(
                                                        requestContext.getLanguage(), dto.name()));
                                    }

                                    // Name is unique, proceed with update
                                    entity.setName(dto.name());
                                    entity.setDescription(dto.description());
                                    return entityDevicesService.update(entity);
                                }));
                    } else {
                        // Name hasn't changed, just update other fields
                        entity.setDescription(dto.description());
                        return entityDevicesService.update(entity);
                    }
                })
                .flatMap(updatedEntity -> {
                    EntityDeviceVM entityVM = EntityDeviceMapper.map.toEntityDeviceVM(updatedEntity);
                    EntityDeviceDetailsVM detailsVM = EntityDeviceMapper.map.toEntityDeviceDetailVM(updatedEntity);

                    // Update both entity and details cache
                    return Uni.combine()
                            .all()
                            .unis(
                                    cacheService.set(cacheKey, entityVM, CACHE_TTL),
                                    cacheService.set(detailsCacheKey, detailsVM, DETAILS_CACHE_TTL))
                            .discardItems()
                            .replaceWith(entityVM)
                            .invoke(() -> {
                                log.debug("Updated cache for entity: {}", id);
                                invalidatePageCache();
                            });
                });
    }

    @Override
    public Uni<EntityDeviceDetailsVM> details(UUID id) {
        String cacheKey = cacheService.generateKey(CACHE_PREFIX_DETAILS, id.toString());

        return cacheService
                .getOrCompute(
                        cacheKey,
                        EntityDeviceDetailsVM.class,
                        () -> entityDevicesService
                                .getById(id)
                                .map(EntityDeviceMapper.map::toEntityDeviceDetailVM)
                                .invoke(() -> log.debug("Loaded entity details from database for ID: {}", id)),
                        DETAILS_CACHE_TTL)
                .invoke(() -> log.debug("Retrieved entity details for ID: {} from cache", id));
    }

    @Override
    public Uni<PageResponse<EntityDeviceVM>> pageable(EntityDeviceFilters filters) {
        // Generate cache key based on filters
        String cacheKey = generatePageCacheKey(filters);

        return cacheService.get(cacheKey, PageResponse.class).flatMap(cachedResult -> {
            if (cachedResult.isPresent()) {
                log.debug("Retrieved page data from cache with key: {}", cacheKey);
                @SuppressWarnings("unchecked")
                PageResponse<EntityDeviceVM> typedResult = (PageResponse<EntityDeviceVM>) cachedResult.get();
                return Uni.createFrom().item(typedResult);
            } else {
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
                                .build())
                        .flatMap(pageResponse -> cacheService
                                .set(cacheKey, pageResponse, Duration.ofMinutes(15))
                                .replaceWith(pageResponse))
                        .invoke(() -> log.debug("Loaded page data from database for filters: {}", filters));
            }
        });
    }

    @Override
    @WithTransaction
    public Uni<Void> delete(UUID id) {
        String cacheKey = cacheService.generateKey(CACHE_PREFIX_ENTITY, id.toString());
        String detailsCacheKey = cacheService.generateKey(CACHE_PREFIX_DETAILS, id.toString());

        return entityDevicesService
                .delete(id)
                .flatMap(ignored -> {
                    // Remove from cache after successful deletion
                    return Uni.combine()
                            .all()
                            .unis(cacheService.delete(cacheKey), cacheService.delete(detailsCacheKey))
                            .discardItems();
                })
                .invoke(() -> {
                    log.debug("Removed entity from cache: {}", id);
                    invalidatePageCache();
                });
    }

    /**
     * Generate cache key for paginated results based on filters
     */
    private String generatePageCacheKey(EntityDeviceFilters filters) {
        return cacheService.generateKey(
                CACHE_PREFIX_PAGE,
                String.valueOf(filters.getPage()),
                String.valueOf(filters.getSize()),
                filters.getName() != null ? filters.getName() : "null",
                filters.getStatus() != null ? filters.getStatus().name() : "null");
    }

    /**
     * Invalidate all page cache entries when entities are modified
     */
    private void invalidatePageCache() {
        String pattern = cacheService.generateKey(CACHE_PREFIX_PAGE, "*");
        cacheService
                .deleteByPattern(pattern)
                .subscribe()
                .with(
                        count -> log.debug("Invalidated {} page cache entries", count),
                        failure -> log.warn("Failed to invalidate page cache", failure));
    }
}
