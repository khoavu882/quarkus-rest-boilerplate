package com.github.kaivu.application.usecase.impl;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.application.service.EntityDevicesService;
import com.github.kaivu.application.usecase.EntityDeviceUseCase;
import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.AsyncObservabilityContext;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.context.TenantObservabilityContext;
import com.github.kaivu.common.mapper.EntityDeviceMapper;
import com.github.kaivu.common.service.ObservabilityService;
import com.github.kaivu.config.ApplicationConfiguration;
import com.github.kaivu.config.annotations.LogExecutionTime;
import com.github.kaivu.config.annotations.Observability;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
@Observability(layer = ObservabilityConstant.LAYER_USECASE)
public class EntityDeviceUseCaseImpl implements EntityDeviceUseCase {

    private final ApplicationConfiguration config;
    private final EntityDevicesService entityDevicesService;
    private final CacheService cacheService;
    private final ObservabilityService observabilityService;
    private final ObservabilityContext observabilityContext;
    private final TenantObservabilityContext tenantContext;
    private final AsyncObservabilityContext asyncContext;

    @Inject
    public EntityDeviceUseCaseImpl(
            ApplicationConfiguration config,
            EntityDevicesService entityDevicesService,
            CacheService cacheService,
            ObservabilityService observabilityService,
            ObservabilityContext observabilityContext,
            TenantObservabilityContext tenantContext,
            AsyncObservabilityContext asyncContext) {
        this.config = config;
        this.entityDevicesService = entityDevicesService;
        this.cacheService = cacheService;
        this.observabilityService = observabilityService;
        this.observabilityContext = observabilityContext;
        this.tenantContext = tenantContext;
        this.asyncContext = asyncContext;
    }

    @Override
    @Observability("create_entity_device")
    @LogExecutionTime
    public Uni<EntityDeviceVM> create(CreateEntityDTO dto) {
        return observabilityService
                .operation("create_entity_device")
                .layer(ObservabilityConstant.LAYER_USECASE)
                .executeUni(entityDevicesService
                        .createWithValidation(dto)
                        .map(EntityDeviceMapper.map::toEntityDeviceVM)
                        .call(ignored -> invalidatePageCacheAsync())
                        .invoke(vm -> log.info(
                                "{}UseCase: Created entity device [id={}, context={}]",
                                tenantContext.getTenantLogPrefix(),
                                vm.getId(),
                                observabilityContext.getContextSummary())));
    }

    @Override
    public Uni<EntityDeviceVM> update(UUID id, UpdateEntityDTO dto) {
        return entityDevicesService
                .updateWithValidation(id, dto)
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
                        () -> entityDevicesService
                                .getByIdWithCaching(id)
                                .map(EntityDeviceMapper.map::toEntityDeviceDetailVM)
                                .invoke(() -> log.debug("UseCase: Loaded entity details from service for ID: {}", id)),
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
                        .unis(
                                entityDevicesService.findWithFilters(filters),
                                entityDevicesService.countWithFilters(filters))
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
    public Uni<Void> delete(UUID id) {
        String detailsCacheKey = cacheService.generateKey(config.cache.prefix.entityDeviceDetails, id.toString());

        return entityDevicesService
                .deleteWithCacheCleanup(id)
                .flatMap(ignored -> cacheService.delete(detailsCacheKey).replaceWithVoid())
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
}
