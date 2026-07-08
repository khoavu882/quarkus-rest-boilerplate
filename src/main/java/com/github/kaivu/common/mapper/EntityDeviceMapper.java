package com.github.kaivu.common.mapper;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.domain.EntityDevice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for EntityDevice domain mappings.
 * Optimized for Quarkus CDI with proper null handling and mapping strategies.
 *
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/10/24
 * Time: 11:08 AM
 */
@Mapper
public interface EntityDeviceMapper {
    EntityDeviceMapper map = Mappers.getMapper(EntityDeviceMapper.class);

    /**
     * Maps EntityDevice to basic EntityDeviceVM
     * Uses custom name mapping to handle domain logic
     */
    @Mapping(target = "name", source = "name")
    EntityDeviceVM toEntityDeviceVM(EntityDevice entityDevice);

    /**
     * Maps EntityDevice to detailed EntityDeviceDetailsVM
     * Includes all audit fields and metadata
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "createdDate", source = "createdDate")
    EntityDeviceDetailsVM toEntityDeviceDetailVM(EntityDevice entityDevice);

    /**
     * Maps CreateEntityDTO to EntityDevice for creation
     * Sets default status and initializes metadata
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVATED")
    @Mapping(target = "metadata", expression = "java(new io.vertx.core.json.JsonObject())")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    EntityDevice toEntity(CreateEntityDTO dto);

    /**
     * Updates existing EntityDevice with UpdateEntityDTO data
     * Preserves audit fields and metadata
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntity(UpdateEntityDTO dto, @MappingTarget EntityDevice entity);
}
