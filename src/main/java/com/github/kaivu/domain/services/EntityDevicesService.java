package com.github.kaivu.domain.services;

import com.github.kaivu.core.entities.EntityDevice;
import com.github.kaivu.infrastructure.services.BaseReadService;
import com.github.kaivu.infrastructure.services.BaseWriteService;

import java.util.UUID;

/**
 * Service Interface for managing {@link EntityDevicesService}.
 */
public interface EntityDevicesService
        extends BaseReadService<EntityDevice, UUID>, BaseWriteService<EntityDevice, UUID> {}
