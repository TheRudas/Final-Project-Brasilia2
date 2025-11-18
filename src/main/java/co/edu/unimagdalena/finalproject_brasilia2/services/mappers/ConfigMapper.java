package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.ConfigCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.ConfigResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.ConfigUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Config;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ConfigMapper {

    Config toEntity(ConfigCreateRequest request);

    ConfigResponse toResponse(Config config);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Config config, ConfigUpdateRequest request);
}
