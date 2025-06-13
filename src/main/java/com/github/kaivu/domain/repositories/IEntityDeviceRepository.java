package com.github.kaivu.domain.repositories;

import com.github.kaivu.core.entities.EntityDevice;
import com.github.kaivu.infrastructure.common.PageableRequest;
import com.github.kaivu.infrastructure.repositories.BaseReadRepository;
import com.github.kaivu.infrastructure.repositories.BaseWriteRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

import java.util.List;
import java.util.UUID;

public interface IEntityDeviceRepository
        extends BaseWriteRepository<EntityDevice>, BaseReadRepository<EntityDevice, UUID> {

    Uni<Tuple2<List<EntityDevice>, Long>> findAll(PageableRequest pageable);
}
