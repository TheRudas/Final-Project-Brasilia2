package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class StopMapperTest {
    private final StopMapper mapper = Mappers.getMapper(StopMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new StopCreateRequest(1L, "Terminal Bogota", 1, 4.6097, -74.0817);
        Stop entity = mapper.toEntity(req);

        assertThat(entity.getName()).isEqualTo("Terminal Bogota");
        assertThat(entity.getOrder()).isEqualTo(1);
        assertThat(entity.getLat()).isEqualTo(4.6097);
        assertThat(entity.getLng()).isEqualTo(-74.0817);
    }

    @Test
    void toResponse_shouldMapEntity() {
        var route = Route.builder().id(2L).build();
        var s = Stop.builder()
                .id(6L).route(route).name("Terminal Medellin")
                .order(5).lat(6.2442).lng(-75.5812).build();

        StopResponse dto = mapper.toResponse(s);

        assertThat(dto.id()).isEqualTo(6L);
        assertThat(dto.routeId()).isEqualTo(2L);
        assertThat(dto.name()).isEqualTo("Terminal Medellin");
        assertThat(dto.order()).isEqualTo(5);
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Stop.builder().id(1L).name("Old Stop")
                .order(1).lat(4.0).lng(-74.0).build();
        var changes = new StopUpdateRequest("New Stop", null, 4.5, null);

        mapper.patch(entity, changes);

        assertThat(entity.getName()).isEqualTo("New Stop");
        assertThat(entity.getOrder()).isEqualTo(1);
        assertThat(entity.getLat()).isEqualTo(4.5);
        assertThat(entity.getLng()).isEqualTo(-74.0);
    }
}