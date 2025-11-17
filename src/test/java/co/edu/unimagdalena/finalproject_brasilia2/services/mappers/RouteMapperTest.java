package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RouteMapperTest {
    private final RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new RouteCreateRequest("RUT-001", "Bogota-Medellin", "Bogota",
                "Medellin", new BigDecimal("400.00"), 360);
        Route entity = mapper.toEntity(req);

        assertThat(entity.getCode()).isEqualTo("RUT-001");
        assertThat(entity.getName()).isEqualTo("Bogota-Medellin");
        assertThat(entity.getDistanceKm()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(entity.getDurationMin()).isEqualTo(360);
    }

    @Test
    void toResponse_shouldMapEntity() {
        var r = Route.builder()
                .id(7L).code("RUT-002").name("Cali-Cartagena")
                .origin("Cali").destination("Cartagena")
                .distanceKm(new BigDecimal("600.00")).durationMin(480).build();

        RouteResponse dto = mapper.toResponse(r);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.code()).isEqualTo("RUT-002");
        assertThat(dto.durationMin()).isEqualTo(480);
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Route.builder().id(1L).name("Old Name")
                .origin("Bogota").destination("Medellin")
                .distanceKm(new BigDecimal("400.00")).durationMin(360).build();
        var changes = new RouteUpdateRequest("New Name", null, null, null, 400);

        mapper.patch(entity, changes);

        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getOrigin()).isEqualTo("Bogota");
        assertThat(entity.getDurationMin()).isEqualTo(400);
    }
}