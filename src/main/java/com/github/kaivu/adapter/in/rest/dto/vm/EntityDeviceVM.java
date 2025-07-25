package com.github.kaivu.adapter.in.rest.dto.vm;

import com.github.kaivu.domain.enumeration.ActionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * View Model for EntityDevice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDeviceVM implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private ActionStatus status;
}
