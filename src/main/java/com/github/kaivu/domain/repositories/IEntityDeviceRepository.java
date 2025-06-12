package com.github.kaivu.domain.repositories;

import com.github.kaivu.core.entities.EntityDevice;
import com.github.kaivu.infrastructure.common.PageableRequest;
import com.github.kaivu.infrastructure.repositories.BaseReadRepository;
import com.github.kaivu.infrastructure.repositories.BaseWriteRepository;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface IEntityDeviceRepository
        extends BaseWriteRepository<EntityDevice>, BaseReadRepository<EntityDevice, UUID> {

    Uni<List<EntityDevice>> findAll(PageableRequest pageable);

    Uni<Long> countAll(PageableRequest pageable);
}
