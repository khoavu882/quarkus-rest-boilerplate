package com.github.kaivu.application.service;

import com.github.kaivu.common.service.BaseReadService;
import com.github.kaivu.common.service.BaseWriteService;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;

import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link EntityDevicesService}.
 */
public interface EntityDevicesService
        extends BaseReadService<EntityDevice, UUID>, BaseWriteService<EntityDevice, UUID> {

    Uni<Optional<EntityDevice>> findByName(String name);

    /**
     * Check exist an Entity Device by Name.
     *
     * @param name of object.
     */
    Uni<EntityDevice> getByName(String name);
}
