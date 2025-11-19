package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TripMapper {

    // ----------- CREATE -----------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "date", source = "localDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "departureTime", source = "departureTime", qualifiedByName = "stringToOffsetDateTime")
    @Mapping(target = "arrivalTime", source = "arrivalTime", qualifiedByName = "stringToOffsetDateTime")
    Trip toEntity(TripDtos.TripCreateRequest dto);


    // ----------- RESPONSE -----------
    @Mapping(source = "route.id", target = "routeId")
    @Mapping(source = "bus.id", target = "busId")
    @Mapping(source = "date", target = "localDate", qualifiedByName = "localDateToString")
    @Mapping(source = "departureTime", target = "departureTime", qualifiedByName = "offsetDateTimeToString")
    @Mapping(source = "arrivalTime", target = "arrivalTime", qualifiedByName = "offsetDateTimeToString")
    TripDtos.TripResponse toTripResponse(Trip trip);

    List<TripDtos.TripResponse> toTripResponseList(List<Trip> trips);


    // ----------- PATCH / UPDATE -----------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "date", source = "localDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "departureTime", source = "departureTime", qualifiedByName = "stringToOffsetDateTime")
    @Mapping(target = "arrivalTime", source = "arrivalTime", qualifiedByName = "stringToOffsetDateTime")
    @Mapping(target = "status", ignore = true)
    void patch(@MappingTarget Trip entity, TripDtos.TripUpdateRequest dto);


    // ----------- MÉTODOS DE CONVERSIÓN -----------

    // String → LocalDate
    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        return date != null ? LocalDate.parse(date) : null;
    }

    // LocalDate → String
    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    // String → OffsetDateTime (CON MANEJO DE ZONA HORARIA)
    @Named("stringToOffsetDateTime")
    default OffsetDateTime stringToOffsetDateTime(String dateTime) {
        if (dateTime == null) return null;

        // Si no tiene zona horaria (+/-HH:MM o Z), agregar UTC por defecto
        if (!dateTime.contains("+") && !dateTime.contains("-") && !dateTime.endsWith("Z")) {
            dateTime += "Z";
        }

        return OffsetDateTime.parse(dateTime);
    }

    // OffsetDateTime → String
    @Named("offsetDateTimeToString")
    default String offsetDateTimeToString(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.toString() : null;
    }
}