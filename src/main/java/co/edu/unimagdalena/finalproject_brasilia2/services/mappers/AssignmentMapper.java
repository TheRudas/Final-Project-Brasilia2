package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Assignment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "dispatcher", ignore = true)
    Assignment toEntity(AssignmentDtos.AssignmentCreateRequest dto);

    // Respuesta: MapStruct mapea por nombre, si los nombres son distintos usa @Mapping
    AssignmentDtos.AssignmentResponse toResponse(Assignment entity);

    // Lista: útil para devolver colecciones en endpoints REST
    List<AssignmentDtos.AssignmentResponse> toResponseList(List<Assignment> entities);

    // Actualización - patch: Ignora campos relacionales y generados que no vengan en el DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "checkListOk", source = "checkListOk")
    void patch(@MappingTarget Assignment entity, AssignmentDtos.AssignmentUpdateRequest dto);


    // Mapeo para listas si lo necesitas:
    // List<AssignmentDtos.AssignmentResponse> toResponseList(List<Assignment> entities);



}
