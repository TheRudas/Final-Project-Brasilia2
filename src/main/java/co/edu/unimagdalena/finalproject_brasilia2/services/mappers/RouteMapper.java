package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {
    @Mapping(target = "id", ignore = true)
    Route toEntity(RouteCreateRequest req);

    RouteResponse toResponse(Route route);

    List<RouteResponse> toResponseList(List<Route> routes);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "destination", source = "destination")
    @Mapping(target = "distanceKm", source = "distanceKm")
    @Mapping(target = "durationMin", source = "durationMin")
    void patch(@MappingTarget Route route, RouteUpdateRequest changes);
}
