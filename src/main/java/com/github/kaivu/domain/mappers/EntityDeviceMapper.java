package com.github.kaivu.domain.mappers;

import com.github.kaivu.core.dto.CreateEntityDTO;
import com.github.kaivu.core.entities.EntityDevice;
import com.github.kaivu.domain.models.EntityDeviceDetailsVM;
import com.github.kaivu.domain.models.EntityDeviceVM;
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
