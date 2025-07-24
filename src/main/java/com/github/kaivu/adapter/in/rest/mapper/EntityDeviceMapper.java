package com.github.kaivu.adapter.in.rest.mapper;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.response.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.response.EntityDeviceVM;
import com.github.kaivu.domain.entities.EntityDevice;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/10/24
 * Time: 11:08 AM
 */
@Mapper
public interface EntityDeviceMapper {
    EntityDeviceMapper map = Mappers.getMapper(EntityDeviceMapper.class);

    EntityDeviceVM toEntityDeviceVM(EntityDevice entityDevice);

    EntityDeviceDetailsVM toEntityDeviceDetailVM(EntityDevice entityDevice);

    EntityDevice toEntity(CreateEntityDTO dto);
}
