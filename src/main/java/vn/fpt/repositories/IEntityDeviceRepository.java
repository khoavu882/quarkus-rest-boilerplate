package vn.fpt.repositories;

import vn.fpt.models.EntityDevice;
import vn.fpt.repositories.base.BaseReadRepository;
import vn.fpt.repositories.base.BaseWriteRepository;

import java.util.UUID;

public interface IEntityDeviceRepository
        extends BaseWriteRepository<EntityDevice>, BaseReadRepository<EntityDevice, UUID> {}
