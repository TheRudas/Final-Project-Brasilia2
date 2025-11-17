package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.StopCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.StopResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.StopUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StopMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    Stop toEntity(StopCreateRequest req);

    @Mapping(source = "route.id", target = "routeId")
    StopResponse toResponse(Stop stop);

    List<StopResponse> toResponseList(List<Stop> stops);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,  ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "order", source = "order")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lng", source = "lng")
    void patch(@MappingTarget Stop stop, StopUpdateRequest changes);

}
