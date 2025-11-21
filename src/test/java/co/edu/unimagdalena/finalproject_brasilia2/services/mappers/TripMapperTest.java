package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TripMapperTest {
    private final TripMapper mapper = Mappers.getMapper(TripMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var localDate = LocalDate.of(2025, 11, 25);
        var departure = OffsetDateTime.now();
        var arrival = OffsetDateTime.now().plusHours(5);

        var req = new TripCreateRequest(1L, 2L, localDate, departure, arrival);
        Trip entity = mapper.toEntity(req);

        assertThat(entity.getDate()).isEqualTo(localDate);
        assertThat(entity.getDepartureTime()).isEqualTo(departure);
        assertThat(entity.getArrivalTime()).isEqualTo(arrival);
        assertThat(entity.getId()).isNull();
        assertThat(entity.getRoute()).isNull();
        assertThat(entity.getBus()).isNull();
        assertThat(entity.getStatus()).isNull();
    }

    @Test
    void toTripResponse_shouldMapEntity() {
        var route = Route.builder().id(10L).build();
        var bus = Bus.builder().id(20L).build();
        var localDate = LocalDate.of(2025, 12, 1);
        var departure = OffsetDateTime.now();
        var arrival = OffsetDateTime.now().plusHours(6);

        var trip = Trip.builder()
                .id(5L)
                .route(route)
                .bus(bus)
                .date(localDate)
                .departureTime(departure)
                .arrivalTime(arrival)
                .status(TripStatus.SCHEDULED)
                .build();

        TripResponse dto = mapper.toTripResponse(trip);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.routeId()).isEqualTo(10L);
        assertThat(dto.busId()).isEqualTo(20L);
        assertThat(dto.localDate()).isEqualTo(localDate);
        assertThat(dto.departureTime()).isEqualTo(departure);
        assertThat(dto.arrivalTime()).isEqualTo(arrival);
        assertThat(dto.status()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var oldDate = LocalDate.of(2025, 11, 20);
        var oldDeparture = OffsetDateTime.now();
        var entity = Trip.builder()
                .id(1L)
                .date(oldDate)
                .departureTime(oldDeparture)
                .build();

        var newDate = LocalDate.of(2025, 11, 22);
        var changes = new TripUpdateRequest(null, null, newDate, null, null, null);

        mapper.patch(entity, changes);

        assertThat(entity.getDate()).isEqualTo(newDate);
        assertThat(entity.getDepartureTime()).isEqualTo(oldDeparture);
    }

    @Test
    void patch_shouldUpdateTimes() {
        var entity = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 11, 20))
                .departureTime(OffsetDateTime.now())
                .arrivalTime(OffsetDateTime.now().plusHours(4))
                .build();

        var newDeparture = OffsetDateTime.now().plusDays(1);
        var newArrival = OffsetDateTime.now().plusDays(1).plusHours(5);
        var changes = new TripUpdateRequest(null, null, null, newDeparture, newArrival, null);

        mapper.patch(entity, changes);

        assertThat(entity.getDepartureTime()).isEqualTo(newDeparture);
        assertThat(entity.getArrivalTime()).isEqualTo(newArrival);
    }
}

