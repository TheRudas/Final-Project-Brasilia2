package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;


import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Parcel;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    // ===================== CREATE =====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    @Mapping(target = "price", ignore = true)         // price lo define el servicio
    @Mapping(target = "status", ignore = true)        // status lo define el servicio
    @Mapping(target = "deliveryOtp", ignore = true)   // otp lo genera el servicio
    Parcel toEntity(ParcelDtos.ParcelCreateRequest dto);


    // ===================== RESPONSE =====================
    @Mapping(target = "fromStopId", source = "fromStop.id")
    @Mapping(target = "toStopId", source = "toStop.id")
    ParcelDtos.ParcelResponse toResponse(Parcel parcel);

    List<ParcelDtos.ParcelResponse> toResponseList(List<Parcel> parcels);


    // ===================== PATCH (UPDATE) =====================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deliveryOtp", ignore = true)
    void patch(@MappingTarget Parcel target, ParcelDtos.ParcelUpdateRequest dto);

}
