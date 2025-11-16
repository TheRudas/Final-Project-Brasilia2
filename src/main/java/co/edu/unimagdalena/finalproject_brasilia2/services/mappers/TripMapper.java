package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TripMapper {

    // ----------- CREATE -----------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true) // porque solo recibes routeId en el dto
    @Mapping(target = "bus", ignore = true)   // porque solo recibes busId en el dto
    Trip toEntity(TripDtos.TripCreateRequest dto);


    // ----------- RESPONSE -----------
    @Mapping(source = "route.id", target = "routeId")
    @Mapping(source = "bus.id", target = "busId")
    TripDtos.TripResponse toTripResponse(Trip trip);

    List<TripDtos.TripResponse> toTripResponseList(List<Trip> trips);


    // ----------- PATCH / UPDATE -----------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "bus", ignore = true)
    void patch(@MappingTarget Trip entity, TripDtos.TripUpdateRequest dto);

}
