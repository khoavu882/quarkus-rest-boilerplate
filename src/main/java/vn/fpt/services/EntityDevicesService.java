package vn.fpt.services;

import io.smallrye.mutiny.Uni;
import vn.fpt.models.EntityDevice;
import vn.fpt.services.dto.CreateEntityDTO;
import vn.fpt.web.vm.EntityDeviceVM;

import java.util.UUID;

/**
 * Service Interface for managing {@link EntityDevicesService}.
 */
public interface EntityDevicesService
        extends BaseReadService<EntityDevice, UUID>, BaseWriteService<EntityDevice, UUID> {

    /**
     * Create Entity Device.
     *
     * @param dto the data of object.
     */
    Uni<EntityDeviceVM> create(CreateEntityDTO dto);
}
