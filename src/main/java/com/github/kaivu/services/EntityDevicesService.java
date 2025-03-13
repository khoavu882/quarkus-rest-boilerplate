package com.github.kaivu.services;

import com.github.kaivu.models.EntityDevice;

import java.util.UUID;

/**
 * Service Interface for managing {@link EntityDevicesService}.
 */
public interface EntityDevicesService
        extends BaseReadService<EntityDevice, UUID>, BaseWriteService<EntityDevice, UUID> {}
