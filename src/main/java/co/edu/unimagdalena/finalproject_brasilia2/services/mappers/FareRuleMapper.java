package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Assignment;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(source = "routeId", target = "route.id")
    @Mapping(source = "fromStopId", target = "fromStop.id")
    @Mapping(source = "toStopId", target = "toStop.id")
    FareRule toEntity(FareRuleDtos.FareRuleCreateRequest dto);


    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(source = "route.id", target = "routeId")
    @Mapping(source = "fromStop.id", target = "fromStopId")
    @Mapping(source = "toStop.id", target = "toStopId")
    FareRuleDtos.FareRuleResponse toResponse(FareRule entity);

    List<FareRuleDtos.FareRuleResponse> toResponseList(List<FareRule> entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(source = "routeId", target = "route.id")
    @Mapping(source = "fromStopId", target = "fromStop.id")
    @Mapping(source = "toStopId", target = "toStop.id")
    void patch(@MappingTarget FareRule target, FareRuleDtos.FareRuleUpdateRequest dto);

}
