package com.github.kaivu.application.port;

import com.github.kaivu.adapter.in.rest.dto.request.PageableRequest;
import com.github.kaivu.common.repositories.BaseReadRepository;
import com.github.kaivu.common.repositories.BaseWriteRepository;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IEntityDeviceRepository
        extends BaseWriteRepository<EntityDevice>, BaseReadRepository<EntityDevice, UUID> {

    Uni<List<EntityDevice>> findAll(PageableRequest pageable);

    Uni<Long> countAll(PageableRequest pageable);

    Uni<Optional<EntityDevice>> findByName(String name);
}
