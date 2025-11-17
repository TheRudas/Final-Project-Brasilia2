package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.IncidentCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.IncidentResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.IncidentUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Incident;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IncidentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Incident toEntity(IncidentCreateRequest request);

    IncidentResponse toResponse(Incident incident);

    List<IncidentResponse> toResponseList(List<Incident> incidents);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "type", source = "type")
    @Mapping(target = "note", source = "note")
    void patch(@MappingTarget Incident target, IncidentUpdateRequest changes);
}