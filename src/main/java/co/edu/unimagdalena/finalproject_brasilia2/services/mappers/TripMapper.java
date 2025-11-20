package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TripMapper {

    // ----------- CREATE -----------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "date", source = "localDate")
    @Mapping(target = "departureTime", source = "departureTime")
    @Mapping(target = "arrivalTime", source = "arrivalTime")
    Trip toEntity(TripDtos.TripCreateRequest dto);


    // ----------- RESPONSE -----------
    @Mapping(source = "route.id", target = "routeId")
    @Mapping(source = "bus.id", target = "busId")
    @Mapping(source = "date", target = "localDate")
    @Mapping(source = "departureTime", target = "departureTime")
    @Mapping(source = "arrivalTime", target = "arrivalTime")
    TripDtos.TripResponse toTripResponse(Trip trip);

    List<TripDtos.TripResponse> toTripResponseList(List<Trip> trips);


    // ----------- PATCH / UPDATE -----------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "date", source = "localDate")
    @Mapping(target = "departureTime", source = "departureTime")
    @Mapping(target = "arrivalTime", source = "arrivalTime")
    @Mapping(target = "status", ignore = true)
    void patch(@MappingTarget Trip entity, TripDtos.TripUpdateRequest dto);
}