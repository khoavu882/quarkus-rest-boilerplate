package vn.fpt.services.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vn.fpt.models.EntityDevice;
import vn.fpt.services.dto.CreateEntityDTO;
import vn.fpt.web.vm.EntityDeviceVM;

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
    EntityDevice toEntity(CreateEntityDTO dto);
}
