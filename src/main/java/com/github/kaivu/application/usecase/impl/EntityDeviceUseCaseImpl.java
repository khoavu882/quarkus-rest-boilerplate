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
import com.github.kaivu.common.mapper.EntityDeviceMapper;
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
public class EntityDeviceUseCaseImpl implements EntityDeviceUseCase {

    private static final String CACHE_PREFIX_DETAILS = "entity_device_details";
    private static final String CACHE_PREFIX_PAGE = "entity_device_page";
    private static final Duration DETAILS_CACHE_TTL = Duration.ofHours(1);

    @Inject
    EntityDevicesService entityDevicesService;

    @Inject
    CacheService cacheService;

    @Override
    public Uni<EntityDeviceVM> create(CreateEntityDTO dto) {
        return entityDevicesService
                .createWithValidation(dto)
                .map(EntityDeviceMapper.map::toEntityDeviceVM)
                .invoke(ignored -> invalidatePageCache())
                .invoke(vm -> log.debug("UseCase: Created entity with ID: {}", vm.getId()));
    }

    @Override
    public Uni<EntityDeviceVM> update(UUID id, UpdateEntityDTO dto) {
        return entityDevicesService
                .updateWithValidation(id, dto)
                .map(EntityDeviceMapper.map::toEntityDeviceVM)
                .invoke(ignored -> invalidatePageCache())
                .invoke(vm -> log.debug("UseCase: Updated entity with ID: {}", id));
    }

    @Override
    public Uni<EntityDeviceDetailsVM> details(UUID id) {
        String cacheKey = cacheService.generateKey(CACHE_PREFIX_DETAILS, id.toString());

        return cacheService
                .getOrCompute(
                        cacheKey,
                        EntityDeviceDetailsVM.class,
                        () -> entityDevicesService
                                .getByIdWithCaching(id)
                                .map(EntityDeviceMapper.map::toEntityDeviceDetailVM)
                                .invoke(() -> log.debug("UseCase: Loaded entity details from service for ID: {}", id)),
                        DETAILS_CACHE_TTL)
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
                PageResponse<EntityDeviceVM> typedResult = (PageResponse<EntityDeviceVM>) cachedResult.get();
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
                                .set(cacheKey, pageResponse, Duration.ofMinutes(15))
                                .replaceWith(pageResponse))
                        .invoke(() -> log.debug("UseCase: Loaded page data from service for filters: {}", filters));
            }
        });
    }

    @Override
    public Uni<Void> delete(UUID id) {
        String detailsCacheKey = cacheService.generateKey(CACHE_PREFIX_DETAILS, id.toString());

        return entityDevicesService
                .deleteWithCacheCleanup(id)
                .flatMap(ignored -> cacheService.delete(detailsCacheKey).replaceWithVoid())
                .invoke(() -> {
                    log.debug("UseCase: Deleted entity and cleared caches: {}", id);
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
