package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class SeatMapperTest {
    private final SeatMapper mapper = Mappers.getMapper(SeatMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new SeatCreateRequest(1L, "A1", SeatType.STANDARD);
        Seat entity = mapper.toEntity(req);

        assertThat(entity.getNumber()).isEqualTo("A1");
        assertThat(entity.getSeatType()).isEqualTo(SeatType.STANDARD);
    }

    @Test
    void toResponse_shouldMapEntity() {
        var bus = Bus.builder().id(5L).build();
        var s = Seat.builder()
                .id(10L).bus(bus).number("B2").seatType(SeatType.PREFERENTIAL).build();

        SeatResponse dto = mapper.toResponse(s);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.busId()).isEqualTo(5L);
        assertThat(dto.number()).isEqualTo("B2");
        assertThat(dto.seatType()).isEqualTo(SeatType.PREFERENTIAL);
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Seat.builder().id(1L).number("A1")
                .seatType(SeatType.STANDARD).build();
        var changes = new SeatUpdateRequest(null, SeatType.PREFERENTIAL);

        mapper.patch(entity, changes);

        assertThat(entity.getNumber()).isEqualTo("A1");
        assertThat(entity.getSeatType()).isEqualTo(SeatType.PREFERENTIAL);
    }
}

