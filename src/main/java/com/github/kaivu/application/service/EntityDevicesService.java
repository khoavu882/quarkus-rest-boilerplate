package com.github.kaivu.application.service;

import com.github.kaivu.common.service.BaseReadService;
import com.github.kaivu.common.service.BaseWriteService;
import com.github.kaivu.domain.EntityDevice;

import java.util.UUID;

/**
 * Service Interface for managing {@link EntityDevicesService}.
 */
public interface EntityDevicesService
        extends BaseReadService<EntityDevice, UUID>, BaseWriteService<EntityDevice, UUID> {}
