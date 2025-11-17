package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.BaggageCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.BaggageResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.BaggageUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Baggage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BaggageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    Baggage toEntity(BaggageCreateRequest request);

    @Mapping(source = "ticket.id", target = "ticketId")
    @Mapping(source = "ticket.passenger.name", target = "passengerName")
    BaggageResponse toResponse(Baggage baggage);

    List<BaggageResponse> toResponseList(List<Baggage> baggage);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "weightKg", source = "weightKg")
    @Mapping(target = "fee", source = "fee")
    void patch(@MappingTarget Baggage target, BaggageUpdateRequest changes);
}