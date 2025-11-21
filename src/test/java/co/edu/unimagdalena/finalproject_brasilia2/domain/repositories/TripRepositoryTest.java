package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TripRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private Route route1;
    private Route route2;
    private Bus bus1;
    private Bus bus2;
    private Trip trip1;
    private Trip trip2;
    private Trip trip3;
    private Trip trip4;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        busRepository.deleteAll();
        routeRepository.deleteAll();

        // Create routes
        route1 = Route.builder()
                .code("R001")
                .name("Bogota-Medellin")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400"))
                .durationMin(480)
                .build();
        route1 = routeRepository.save(route1);

        route2 = Route.builder()
                .code("R002")
                .name("Cali-Cartagena")
                .origin("Cali")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("700"))
                .durationMin(600)
                .build();
        route2 = routeRepository.save(route2);

        // Create buses
        bus1 = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();
        bus1 = busRepository.save(bus1);

        bus2 = Bus.builder()
                .plate("XYZ789")
                .capacity(50)
                .status(true)
                .build();
        bus2 = busRepository.save(bus2);

        LocalDate today = LocalDate.now();
        OffsetDateTime now = OffsetDateTime.now();

        // Create trips
        trip1 = Trip.builder()
                .route(route1)
                .bus(bus1)
                .date(today.plusDays(1))
                .departureTime(now.plusDays(1))
                .arrivalTime(now.plusDays(1).plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();

        trip2 = Trip.builder()
                .route(route1)
                .bus(bus1)
                .date(today.plusDays(2))
                .departureTime(now.plusDays(2))
                .arrivalTime(now.plusDays(2).plusHours(8))
                .status(TripStatus.BOARDING)
                .build();

        trip3 = Trip.builder()
                .route(route2)
                .bus(bus2)
                .date(today.plusDays(1))
                .departureTime(now.plusDays(1).plusHours(2))
                .arrivalTime(now.plusDays(1).plusHours(12))
                .status(TripStatus.SCHEDULED)
                .build();

        trip4 = Trip.builder()
                .route(route1)
                .bus(bus1)
                .date(today.plusDays(1))
                .departureTime(now.plusDays(1).plusHours(10))
                .arrivalTime(now.plusDays(1).plusHours(18))
                .status(TripStatus.CANCELLED)
                .build();
    }

    @Test
    @DisplayName("Trip: find by status")
    void shouldFindByStatus() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);
        tripRepository.save(trip4);

        // When
        var scheduledTrips = tripRepository.findByStatus(TripStatus.SCHEDULED);
        var cancelledTrips = tripRepository.findByStatus(TripStatus.CANCELLED);

        // Then
        assertThat(scheduledTrips).hasSize(2);
        assertThat(cancelledTrips).hasSize(1);
    }

    @Test
    @DisplayName("Trip: find by bus id")
    void shouldFindByBusId() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);
        tripRepository.save(trip4);

        // When
        var bus1Trips = tripRepository.findByBusId(bus1.getId());
        var bus2Trips = tripRepository.findByBusId(bus2.getId());

        // Then
        assertThat(bus1Trips).hasSize(3);
        assertThat(bus2Trips).hasSize(1);
    }

    @Test
    @DisplayName("Trip: find by route id")
    void shouldFindByRouteId() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);
        tripRepository.save(trip4);

        // When
        var route1Trips = tripRepository.findByRouteId(route1.getId());
        var route2Trips = tripRepository.findByRouteId(route2.getId());

        // Then
        assertThat(route1Trips).hasSize(3);
        assertThat(route2Trips).hasSize(1);
    }

    @Test
    @DisplayName("Trip: find by status and bus id")
    void shouldFindByStatusAndBusId() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip4);

        // When
        var result = tripRepository.findByStatusAndBusId(TripStatus.SCHEDULED, bus1.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Trip: find by date")
    void shouldFindByDate() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);
        tripRepository.save(trip4);

        LocalDate targetDate = LocalDate.now().plusDays(1);

        // When
        var result = tripRepository.findByDate(targetDate);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Trip: find by route id and date")
    void shouldFindByRouteIdAndDate() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip4);

        LocalDate targetDate = LocalDate.now().plusDays(1);

        // When
        var result = tripRepository.findByRouteIdAndDate(route1.getId(), targetDate);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Trip: find by route id, date and status")
    void shouldFindByRouteIdAndDateAndStatus() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip4);

        LocalDate targetDate = LocalDate.now().plusDays(1);

        // When
        var result = tripRepository.findByRouteIdAndDateAndStatus(
                route1.getId(),
                targetDate,
                TripStatus.SCHEDULED
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Trip: find by bus id and date")
    void shouldFindByBusIdAndDate() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip4);

        LocalDate targetDate = LocalDate.now().plusDays(1);

        // When
        var result = tripRepository.findByBusIdAndDate(bus1.getId(), targetDate);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Trip: find by bus id, date and status in")
    void shouldFindByBusIdAndDateAndStatusIn() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip4);

        LocalDate targetDate = LocalDate.now().plusDays(1);
        List<TripStatus> statuses = List.of(TripStatus.SCHEDULED, TripStatus.BOARDING);

        // When
        var result = tripRepository.findByBusIdAndDateAndStatusIn(
                bus1.getId(),
                targetDate,
                statuses
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Trip: find active trips by bus and date")
    void shouldFindActiveTripsByBusAndDate() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip4);

        LocalDate targetDate = LocalDate.now().plusDays(1);

        // When
        var result = tripRepository.findActiveTripsByBusAndDate(bus1.getId(), targetDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Trip: find available trips")
    void shouldFindAvailableTrips() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip4);

        LocalDate targetDate = LocalDate.now().plusDays(1);
        OffsetDateTime now = OffsetDateTime.now();

        // When
        var result = tripRepository.findAvailableTrips(route1.getId(), targetDate, now);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Trip: find by date between")
    void shouldFindByDateBetween() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);

        // When
        var result = tripRepository.findByDateBetween(start, end);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Trip: find by route id and date between")
    void shouldFindByRouteIdAndDateBetween() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);

        // When
        var result = tripRepository.findByRouteIdAndDateBetween(route1.getId(), start, end);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Trip: count by status")
    void shouldCountByStatus() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);
        tripRepository.save(trip4);

        // When
        var scheduledCount = tripRepository.countByStatus(TripStatus.SCHEDULED);
        var boardingCount = tripRepository.countByStatus(TripStatus.BOARDING);
        var cancelledCount = tripRepository.countByStatus(TripStatus.CANCELLED);

        // Then
        assertThat(scheduledCount).isEqualTo(2L);
        assertThat(boardingCount).isEqualTo(1L);
        assertThat(cancelledCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("Trip: count by bus id and date")
    void shouldCountByBusIdAndDate() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip4);

        LocalDate targetDate = LocalDate.now().plusDays(1);

        // When
        var count = tripRepository.countByBusIdAndDate(bus1.getId(), targetDate);

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Trip: check if exists by route id")
    void shouldCheckExistsTripByRouteId() {
        // Given
        tripRepository.save(trip1);

        // When
        var exists = tripRepository.existsTripByRouteId(route1.getId());
        var notExists = tripRepository.existsTripByRouteId(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Trip: check if exists by bus id")
    void shouldCheckExistsByBusId() {
        // Given
        tripRepository.save(trip1);

        // When
        var exists = tripRepository.existsByBusId(bus1.getId());
        var notExists = tripRepository.existsByBusId(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Trip: check if exists by bus id, date and status in")
    void shouldCheckExistsByBusIdAndDateAndStatusIn() {
        // Given
        tripRepository.save(trip1);

        LocalDate targetDate = LocalDate.now().plusDays(1);
        List<TripStatus> statuses = List.of(TripStatus.SCHEDULED, TripStatus.BOARDING);

        // When
        var exists = tripRepository.existsByBusIdAndDateAndStatusIn(
                bus1.getId(),
                targetDate,
                statuses
        );

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Trip: find upcoming trips")
    void shouldFindUpcomingTrips() {
        // Given
        tripRepository.save(trip1);
        tripRepository.save(trip2);

        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().plusDays(3);

        // When
        var result = tripRepository.findUpcomingTrips(start, end);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Trip: find delayed trips")
    void shouldFindDelayedTrips() {
        // Given
        Trip delayedTrip = Trip.builder()
                .route(route1)
                .bus(bus1)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().minusHours(10))
                .arrivalTime(OffsetDateTime.now().minusHours(2))
                .status(TripStatus.DEPARTED)
                .build();
        tripRepository.save(delayedTrip);

        // When
        var result = tripRepository.findDelayedTrips(OffsetDateTime.now());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.DEPARTED);
    }

    @Test
    @DisplayName("Trip: find conflicting trips")
    void shouldFindConflictingTrips() {
        // Given
        tripRepository.save(trip1);

        LocalDate date = LocalDate.now().plusDays(1);
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1).plusHours(1);
        OffsetDateTime arrival = OffsetDateTime.now().plusDays(1).plusHours(9);

        // When
        var result = tripRepository.findConflictingTrips(
                bus1.getId(),
                date,
                departure,
                arrival
        );

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Trip: find boarding trips")
    void shouldFindBoardingTrips() {
        // Given
        tripRepository.save(trip2);

        OffsetDateTime maxDeparture = OffsetDateTime.now().plusDays(3);

        // When
        var result = tripRepository.findBoardingTrips(maxDeparture);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TripStatus.BOARDING);
    }

    @Test
    @DisplayName("Trip: return empty list when status has no trips")
    void shouldReturnEmptyWhenStatusHasNoTrips() {
        // Given
        tripRepository.save(trip1);

        // When
        var result = tripRepository.findByStatus(TripStatus.ARRIVED);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Trip: return empty list when date has no trips")
    void shouldReturnEmptyWhenDateHasNoTrips() {
        // Given
        tripRepository.save(trip1);

        // When
        var result = tripRepository.findByDate(LocalDate.now().plusDays(10));

        // Then
        assertThat(result).isEmpty();
    }
}