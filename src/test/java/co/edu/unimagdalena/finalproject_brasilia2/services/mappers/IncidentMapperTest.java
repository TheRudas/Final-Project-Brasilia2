package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentMapperTest {
    private final IncidentMapper mapper = Mappers.getMapper(IncidentMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new IncidentCreateRequest(IncidentEntityType.TRIP, 5L, IncidentType.SECURITY, "Ese vale esta raro");
        Incident entity = mapper.toEntity(req);

        assertThat(entity.getEntityType()).isEqualTo(IncidentEntityType.TRIP);
        assertThat(entity.getEntityId()).isEqualTo(5L);
        assertThat(entity.getType()).isEqualTo(IncidentType.SECURITY);
        assertThat(entity.getNote()).isEqualTo("Ese vale esta raro");
    }

    @Test
    void toResponse_shouldMapEntity() {
        var i = Incident.builder()
                .id(3L).entityType(IncidentEntityType.TRIP).entityId(10L)
                .type(IncidentType.VEHICLE).note("Se espichó la cadena")
                .createdAt(OffsetDateTime.now()).build();

        IncidentResponse dto = mapper.toResponse(i);

        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.entityType()).isEqualTo(IncidentEntityType.TRIP);
        assertThat(dto.entityId()).isEqualTo(10L);
        assertThat(dto.type()).isEqualTo(IncidentType.VEHICLE);
        assertThat(dto.note()).isEqualTo("Se espichó la cadena");
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Incident.builder().id(1L).type(IncidentType.VEHICLE)
                .note("Old note").build();
        var changes = new IncidentUpdateRequest("Updated note", null);

        mapper.patch(entity, changes);

        assertThat(entity.getType()).isEqualTo(IncidentType.VEHICLE);
        assertThat(entity.getNote()).isEqualTo("Updated note");
    }
}