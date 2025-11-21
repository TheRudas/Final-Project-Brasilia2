package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;


import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusMapper {
    @Mapping(target = "plate", source = "licensePlate")
    @Mapping(target = "id", ignore = true) // BusCreateRequest nunca tendrá id
    @Mapping(target = "amenities", ignore = true) // si no lo usas en el DTO
    @Mapping(target = "capacity", source = "capacity")// si status no viene, usa true
    @Mapping(target = "status", source = "status", defaultValue = "true")// si status no viene, usa true
    Bus toEntity(BusDtos.BusCreateRequest dto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "licensePlate", source = "plate")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "capacity", source = "capacity")// si status no viene, usa true
    @Mapping(target = "status", source = "status", defaultValue = "true")// si status n

    BusDtos.BusResponse toResponse(Bus  dto);

    List<BusDtos.BusResponse> toResponseEntityList(List<Bus> entities);

    @Mapping(target ="amenities", ignore = true) // si no lo usas en el DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plate", source = "licensePlate")
    @Mapping(target = "capacity", source = "capacity")// si status no viene, usa true
    @Mapping(target = "status", source = "status", defaultValue = "true")// si status no viene, usa true
    Bus toUpdateEntity(BusDtos.BusUpdateRequest dto);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "plate", source = "licensePlate")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "status", source = "status")
    void patch(@MappingTarget Bus target, BusDtos.BusUpdateRequest changes);
}
