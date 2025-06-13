package com.github.kaivu.core.usecase;

import com.github.kaivu.core.dto.CreateEntityDTO;
import com.github.kaivu.core.dto.UpdateEntityDTO;
import com.github.kaivu.domain.models.EntityDeviceDetailsVM;
import com.github.kaivu.domain.models.EntityDeviceFilters;
import com.github.kaivu.domain.models.EntityDeviceVM;
import com.github.kaivu.infrastructure.common.PageResponse;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/13/25
 * Time: 11:47 AM
 */
public interface EntityDeviceUseCase {

    /**
     * Create Entity Device.
     *
     * @param dto the data of object.
     */
    Uni<EntityDeviceVM> create(CreateEntityDTO dto);

    /**
     * Create Entity Device.
     *
     * @param dto the data of object.
     */
    Uni<EntityDeviceVM> update(UUID id, UpdateEntityDTO dto);

    /**
     * Get details an Entity Device by ID.
     *
     * @param id of object.
     */
    Uni<EntityDeviceDetailsVM> details(UUID id);

    /**
     * Pageable Entity Devices.
     *
     * @param filters of object.
     */
    Uni<PageResponse<EntityDeviceVM>> pageable(EntityDeviceFilters filters);

    /**
     * Detele an Entity Device by ID.
     *
     * @param id of object.
     */
    Uni<Void> delete(UUID id);
}
