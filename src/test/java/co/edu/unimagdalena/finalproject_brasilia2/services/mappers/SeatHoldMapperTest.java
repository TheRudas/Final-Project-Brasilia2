package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SeatHoldMapperTest {
    private final SeatHoldMapper mapper = Mappers.getMapper(SeatHoldMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new SeatHoldCreateRequest(1L, "C3", 2L);
        SeatHold entity = mapper.toEntity(req);

        assertThat(entity.getSeatNumber()).isEqualTo("C3");
        assertThat(entity.getStatus()).isEqualTo(SeatHoldStatus.HOLD);
    }

    @Test
    void toResponse_shouldMapEntity() {
        var trip = Trip.builder().id(8L).build();
        var user = User.builder().id(3L).name("Maria Lopez").build();
        var sh = SeatHold.builder()
                .id(15L).trip(trip).user(user).seatNumber("D4")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(10)).build();

        SeatHoldResponse dto = mapper.toResponse(sh);

        assertThat(dto.id()).isEqualTo(15L);
        assertThat(dto.tripId()).isEqualTo(8L);
        assertThat(dto.userId()).isEqualTo(3L);
        assertThat(dto.userName()).isEqualTo("Maria Lopez");
        assertThat(dto.seatNumber()).isEqualTo("D4");
    }
}