package com.github.kaivu.adapter.in.rest.dto.vm;

import com.github.kaivu.domain.enumeration.ActionStatus;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Detailed View Model for EntityDevice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDeviceDetailsVM implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String description;
    private ActionStatus status;
    private JsonObject metadata = new JsonObject();
    private Instant createdDate;
}
