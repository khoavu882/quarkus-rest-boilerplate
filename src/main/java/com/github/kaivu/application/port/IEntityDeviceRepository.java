package com.github.kaivu.application.port;

import com.github.kaivu.adapter.in.rest.dto.request.PageableRequest;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IEntityDeviceRepository {

    // Read operations
    Uni<Optional<EntityDevice>> findById(UUID identity);

    Uni<List<EntityDevice>> findByIds(List<UUID> identities);

    Uni<Optional<EntityDevice>> findByName(String name);

    Uni<List<EntityDevice>> findAll(PageableRequest pageable);

    Uni<Long> countAll(PageableRequest pageable);

    // Write operations
    Uni<EntityDevice> persist(EntityDevice entity);

    Uni<List<EntityDevice>> persist(List<EntityDevice> entities);

    Uni<EntityDevice> update(EntityDevice entity);

    Uni<List<EntityDevice>> update(List<EntityDevice> entities);

    Uni<Void> delete(EntityDevice entity);
}
