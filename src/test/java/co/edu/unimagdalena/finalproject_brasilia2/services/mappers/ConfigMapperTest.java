package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Config;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigMapperTest {
    private final ConfigMapper mapper = Mappers.getMapper(ConfigMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new ConfigCreateRequest("hold_timeout_minutes", "10");
        Config entity = mapper.toEntity(req);

        assertThat(entity.getKey()).isEqualTo("hold_timeout_minutes");
        assertThat(entity.getValue()).isEqualTo("10");
    }

    @Test
    void toResponse_shouldMapEntity() {
        var config = Config.builder()
                .key("baggage_weight_limit_kg")
                .value("25")
                .build();

        ConfigResponse dto = mapper.toResponse(config);

        assertThat(dto.key()).isEqualTo("baggage_weight_limit_kg");
        assertThat(dto.value()).isEqualTo("25");
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Config.builder()
                .key("cancellation_fee_percent")
                .value("15")
                .build();
        var changes = new ConfigUpdateRequest("20");

        mapper.patch(entity, changes);

        assertThat(entity.getKey()).isEqualTo("cancellation_fee_percent");
        assertThat(entity.getValue()).isEqualTo("20");
    }
}

