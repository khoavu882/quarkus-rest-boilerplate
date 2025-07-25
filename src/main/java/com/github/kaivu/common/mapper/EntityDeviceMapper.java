package com.github.kaivu.common.mapper;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.domain.EntityDevice;
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
