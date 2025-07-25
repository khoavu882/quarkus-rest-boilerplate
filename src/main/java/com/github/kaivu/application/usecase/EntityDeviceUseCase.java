package com.github.kaivu.application.usecase;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
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
