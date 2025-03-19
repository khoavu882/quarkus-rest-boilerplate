package com.github.kaivu.repositories;

import com.github.kaivu.models.EntityDevice;
import com.github.kaivu.repositories.base.BaseReadRepository;
import com.github.kaivu.repositories.base.BaseWriteRepository;
import com.github.kaivu.web.vm.common.PageableRequest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

import java.util.List;
import java.util.UUID;

public interface IEntityDeviceRepository
        extends BaseWriteRepository<EntityDevice>, BaseReadRepository<EntityDevice, UUID> {

    Uni<Tuple2<List<EntityDevice>, Long>> findAll(PageableRequest pageable);
}
