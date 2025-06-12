package com.github.kaivu.domain.mappers;

import com.github.kaivu.core.dto.CreateEntityDTO;
import com.github.kaivu.core.entities.EntityDevice;
import com.github.kaivu.domain.models.EntityDeviceDetailsVM;
import com.github.kaivu.domain.models.EntityDeviceVM;
import org.mapstruct.BeanMapping;
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

    @BeanMapping(ignoreByDefault = true)
    EntityDeviceVM toEntityDeviceVM(EntityDevice entityDevice);

    @BeanMapping(ignoreByDefault = true)
    EntityDeviceDetailsVM toEntityDeviceDetailVM(EntityDevice entityDevice);

    @BeanMapping(ignoreByDefault = true)
    EntityDevice toEntity(CreateEntityDTO dto);
}
