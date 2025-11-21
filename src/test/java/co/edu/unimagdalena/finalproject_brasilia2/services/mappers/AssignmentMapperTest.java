package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentMapperTest {
    private final AssignmentMapper mapper = Mappers.getMapper(AssignmentMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new AssignmentCreateRequest(1L, 2L, 3L, true);
        Assignment entity = mapper.toEntity(req);

        assertThat(entity.isCheckListOk()).isTrue();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getAssignedAt()).isNull();
        assertThat(entity.getTrip()).isNull();
        assertThat(entity.getDriver()).isNull();
        assertThat(entity.getDispatcher()).isNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        var trip = Trip.builder().id(10L).build();
        var driver = User.builder().id(20L).name("Juan Driver").build();
        var dispatcher = User.builder().id(30L).name("Pedro Dispatcher").build();
        var now = OffsetDateTime.now();

        var assignment = Assignment.builder()
                .id(5L)
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(now)
                .build();

        AssignmentResponse dto = mapper.toResponse(assignment);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.tripId()).isEqualTo(10L);
        assertThat(dto.driverId()).isEqualTo(20L);
        assertThat(dto.dispatcherId()).isEqualTo(30L);
        assertThat(dto.checkListOk()).isTrue();
        assertThat(dto.assignedAt()).isEqualTo(now);
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Assignment.builder()
                .id(1L)
                .checkListOk(false)
                .build();
        var changes = new AssignmentUpdateRequest(null, null, true);

        mapper.patch(entity, changes);

        assertThat(entity.isCheckListOk()).isTrue();
    }

    @Test
    void patch_shouldUpdateDriverAndDispatcher() {
        var entity = Assignment.builder()
                .id(1L)
                .checkListOk(true)
                .build();
        var changes = new AssignmentUpdateRequest(99L, 88L, false);

        mapper.patch(entity, changes);

        assertThat(entity.isCheckListOk()).isFalse();
    }
}

