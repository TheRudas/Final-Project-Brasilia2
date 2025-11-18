package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Ticket;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    //For avoid mapping errors with Longs and full entities, Claude the Rooster, recommends to me assign them in Services later. I don't know to do this Gamero, have faith.
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "passenger", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    Ticket toEntity(TicketDtos.TicketCreateRequest req);

    @Mapping(source = "trip.id", target = "tripId")
    @Mapping(source = "fromStop.id", target = "fromStopId")
    @Mapping(source = "toStop.id", target = "toStopId")
    @Mapping(source = "passenger.id", target = "passengerId")
    @Mapping(target = "refundAmount", source = "refundAmount")
    //I want to add some extra data to ticket
    @Mapping(source = "passenger.name", target = "passengerName")
    @Mapping(source = "trip.bus.plate", target = "busPlate")
    @Mapping(source = "trip.departureTime", target = "departureAt")
    //endAdd
    TicketDtos.TicketResponse toResponse(Ticket ticket);

    List<TicketDtos.TicketResponse> toResponseList(List<Ticket> tickets);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", source = "status")
    void patch(@MappingTarget Ticket ticket, TicketDtos.TicketUpdateRequest req);
}
