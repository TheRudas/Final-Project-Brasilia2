package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.SeatHold;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatHoldMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "status", constant = "HOLD")
    SeatHold toEntity(SeatHoldCreateRequest request);

    @Mapping(source = "trip.id", target = "tripId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    SeatHoldResponse toResponse(SeatHold seatHold);

    List<SeatHoldResponse> toResponseList(List<SeatHold> seatHolds);
}
