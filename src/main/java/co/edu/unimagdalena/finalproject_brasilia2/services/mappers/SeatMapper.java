package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Seat;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bus", ignore = true)
    Seat toEntity(SeatDtos.SeatCreateRequest req);

    @Mapping(source = "bus.id", target = "busId")
    SeatDtos.SeatResponse toResponse(Seat seat);

    List<SeatDtos.SeatResponse> toResponseList(List<Seat> seats);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "number", source = "number")
    @Mapping(target = "seatType", source = "seatType")
    void patch(@MappingTarget Seat seat, SeatDtos.SeatUpdateRequest changes);
}
