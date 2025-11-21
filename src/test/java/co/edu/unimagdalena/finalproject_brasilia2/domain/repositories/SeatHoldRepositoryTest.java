package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatHoldRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private User user1;
    private User user2;
    private Trip trip1;
    private Trip trip2;
    private SeatHold seatHold1;
    private SeatHold seatHold2;
    private SeatHold seatHold3;
    private SeatHold seatHold4;

    @BeforeEach
    void setUp() {
        seatHoldRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        busRepository.deleteAll();
        routeRepository.deleteAll();

        // Create route
        Route route = Route.builder()
                .code("R001")
                .name("Bogota-Cali")
                .origin("Bogota")
                .destination("Cali")
                .distanceKm(new BigDecimal("450"))
                .durationMin(480)
                .build();
        route = routeRepository.save(route);

        // Create bus
        Bus bus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();
        bus = busRepository.save(bus);

        // Create trips
        trip1 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(OffsetDateTime.now().plusDays(1))
                .arrivalTime(OffsetDateTime.now().plusDays(1).plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();
        trip1 = tripRepository.save(trip1);

        trip2 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(2))
                .departureTime(OffsetDateTime.now().plusDays(2))
                .arrivalTime(OffsetDateTime.now().plusDays(2).plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();
        trip2 = tripRepository.save(trip2);

        // Create users
        user1 = User.builder()
                .name("Juan Perez")
                .email("juan@mail.com")
                .phone("3001234567")
                .passwordHash("hash123")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .name("Maria Garcia")
                .email("maria@mail.com")
                .phone("3107654321")
                .passwordHash("hash456")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        user2 = userRepository.save(user2);

        OffsetDateTime now = OffsetDateTime.now();

        // Create seat holds
        seatHold1 = SeatHold.builder()
                .trip(trip1)
                .seatNumber("A1")
                .user(user1)
                .expiresAt(now.plusMinutes(15))
                .status(SeatHoldStatus.HOLD)
                .build();

        seatHold2 = SeatHold.builder()
                .trip(trip1)
                .seatNumber("A2")
                .user(user1)
                .expiresAt(now.plusMinutes(10))
                .status(SeatHoldStatus.HOLD)
                .build();

        seatHold3 = SeatHold.builder()
                .trip(trip1)
                .seatNumber("B1")
                .user(user2)
                .expiresAt(now.minusMinutes(5))
                .status(SeatHoldStatus.HOLD)
                .build();

        seatHold4 = SeatHold.builder()
                .trip(trip2)
                .seatNumber("A1")
                .user(user1)
                .expiresAt(now.plusMinutes(20))
                .status(SeatHoldStatus.EXPIRED)
                .build();
    }

    @Test
    @DisplayName("SeatHold: find by trip id")
    void shouldFindByTripId() {
        // Given
        seatHoldRepository.save(seatHold1);
        seatHoldRepository.save(seatHold2);
        seatHoldRepository.save(seatHold3);
        seatHoldRepository.save(seatHold4);

        // When
        var trip1Holds = seatHoldRepository.findByTripId(trip1.getId());
        var trip2Holds = seatHoldRepository.findByTripId(trip2.getId());

        // Then
        assertThat(trip1Holds).hasSize(3);
        assertThat(trip1Holds)
                .extracting(SeatHold::getSeatNumber)
                .containsExactlyInAnyOrder("A1", "A2", "B1");

        assertThat(trip2Holds).hasSize(1);
        assertThat(trip2Holds.get(0).getSeatNumber()).isEqualTo("A1");
    }

    @Test
    @DisplayName("SeatHold: find by user id")
    void shouldFindByUserId() {
        // Given
        seatHoldRepository.save(seatHold1);
        seatHoldRepository.save(seatHold2);
        seatHoldRepository.save(seatHold3);
        seatHoldRepository.save(seatHold4);

        // When
        var user1Holds = seatHoldRepository.findByUserId(user1.getId());
        var user2Holds = seatHoldRepository.findByUserId(user2.getId());

        // Then
        assertThat(user1Holds).hasSize(3);
        assertThat(user1Holds)
                .extracting(SeatHold::getSeatNumber)
                .containsExactlyInAnyOrder("A1", "A2", "A1");

        assertThat(user2Holds).hasSize(1);
        assertThat(user2Holds.get(0).getSeatNumber()).isEqualTo("B1");
    }

    @Test
    @DisplayName("SeatHold: find by user id and trip id")
    void shouldFindByUserIdAndTripId() {
        // Given
        seatHoldRepository.save(seatHold1);
        seatHoldRepository.save(seatHold2);
        seatHoldRepository.save(seatHold3);

        // When
        var result = seatHoldRepository.findByUserIdAndTripId(user1.getId(), trip1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(SeatHold::getSeatNumber)
                .containsExactlyInAnyOrder("A1", "A2");
    }

    @Test
    @DisplayName("SeatHold: check if exists by user id and trip id")
    void shouldCheckExistsByUserIdAndTripId() {
        // Given
        seatHoldRepository.save(seatHold1);

        // When
        var exists = seatHoldRepository.existsByUserIdAndTripId(user1.getId(), trip1.getId());
        var notExists = seatHoldRepository.existsByUserIdAndTripId(user2.getId(), trip2.getId());

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("SeatHold: check if exists seat hold by id")
    void shouldCheckExistsSeatHoldById() {
        // Given
        seatHold1 = seatHoldRepository.save(seatHold1);

        // When
        var exists = seatHoldRepository.existsSeatHoldById(seatHold1.getId());
        var notExists = seatHoldRepository.existsSeatHoldById(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("SeatHold: find by trip id, seat number and status")
    void shouldFindByTripIdAndSeatNumberAndStatus() {
        // Given
        seatHoldRepository.save(seatHold1);
        seatHoldRepository.save(seatHold4);

        // When
        var holdResult = seatHoldRepository.findByTripIdAndSeatNumberAndStatus(
                trip1.getId(),
                "A1",
                SeatHoldStatus.HOLD
        );
        var expiredResult = seatHoldRepository.findByTripIdAndSeatNumberAndStatus(
                trip2.getId(),
                "A1",
                SeatHoldStatus.EXPIRED
        );

        // Then
        assertThat(holdResult).isPresent();
        assertThat(holdResult.get().getUser().getName()).isEqualTo("Juan Perez");

        assertThat(expiredResult).isPresent();
        assertThat(expiredResult.get().getStatus()).isEqualTo(SeatHoldStatus.EXPIRED);
    }

    @Test
    @DisplayName("SeatHold: find by status and expires at before")
    void shouldFindByStatusAndExpiresAtBefore() {
        // Given
        seatHoldRepository.save(seatHold1);
        seatHoldRepository.save(seatHold2);
        seatHoldRepository.save(seatHold3);

        OffsetDateTime now = OffsetDateTime.now();

        // When
        var result = seatHoldRepository.findByStatusAndExpiresAtBefore(SeatHoldStatus.HOLD, now);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeatNumber()).isEqualTo("B1");
    }

    @Test
    @DisplayName("SeatHold: find expired holds with custom query")
    void shouldFindExpiredHoldsWithCustomQuery() {
        // Given
        seatHoldRepository.save(seatHold1);
        seatHoldRepository.save(seatHold2);
        seatHoldRepository.save(seatHold3);

        OffsetDateTime now = OffsetDateTime.now();

        // When
        var result = seatHoldRepository.findExpiredHolds(SeatHoldStatus.HOLD, now);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeatNumber()).isEqualTo("B1");
        assertThat(result.get(0).getExpiresAt()).isBefore(now);
    }

    @Test
    @DisplayName("SeatHold: return empty list when trip has no holds")
    void shouldReturnEmptyWhenTripHasNoHolds() {
        // Given - trip without holds
        Route route = Route.builder()
                .code("R002")
                .name("Medellin-Cartagena")
                .origin("Medellin")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("650"))
                .durationMin(600)
                .build();
        route = routeRepository.save(route);

        Trip trip3 = Trip.builder()
                .route(route)
                .bus(trip1.getBus())
                .date(LocalDate.now().plusDays(3))
                .departureTime(OffsetDateTime.now().plusDays(3))
                .arrivalTime(OffsetDateTime.now().plusDays(3).plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();
        trip3 = tripRepository.save(trip3);

        // When
        var result = seatHoldRepository.findByTripId(trip3.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SeatHold: return empty list when user has no holds")
    void shouldReturnEmptyWhenUserHasNoHolds() {
        // Given - user without holds
        User user3 = User.builder()
                .name("Carlos Test")
                .email("carlos@mail.com")
                .phone("3009876543")
                .passwordHash("hash789")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        user3 = userRepository.save(user3);

        // When
        var result = seatHoldRepository.findByUserId(user3.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SeatHold: return empty when trip, seat and status combination not found")
    void shouldReturnEmptyWhenTripSeatStatusNotMatch() {
        // Given
        seatHoldRepository.save(seatHold1);

        // When
        var result = seatHoldRepository.findByTripIdAndSeatNumberAndStatus(
                trip1.getId(),
                "Z99",
                SeatHoldStatus.HOLD
        );

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SeatHold: return empty list when no expired holds")
    void shouldReturnEmptyWhenNoExpiredHolds() {
        // Given
        seatHoldRepository.save(seatHold1);
        seatHoldRepository.save(seatHold2);

        OffsetDateTime now = OffsetDateTime.now();

        // When
        var result = seatHoldRepository.findExpiredHolds(SeatHoldStatus.HOLD, now);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SeatHold: verify expires at ordering in custom query")
    void shouldVerifyExpiresAtOrderingInCustomQuery() {
        // Given
        seatHold3 = SeatHold.builder()
                .trip(trip1)
                .seatNumber("B1")
                .user(user2)
                .expiresAt(OffsetDateTime.now().minusMinutes(10))
                .status(SeatHoldStatus.HOLD)
                .build();

        SeatHold seatHold5 = SeatHold.builder()
                .trip(trip1)
                .seatNumber("C1")
                .user(user2)
                .expiresAt(OffsetDateTime.now().minusMinutes(5))
                .status(SeatHoldStatus.HOLD)
                .build();

        seatHoldRepository.save(seatHold3);
        seatHoldRepository.save(seatHold5);

        OffsetDateTime now = OffsetDateTime.now();

        // When
        var result = seatHoldRepository.findExpiredHolds(SeatHoldStatus.HOLD, now);

        // Then
        assertThat(result).hasSize(2);
        // Should be ordered by expiresAt ascending (oldest first)
        assertThat(result.get(0).getSeatNumber()).isEqualTo("B1");
        assertThat(result.get(1).getSeatNumber()).isEqualTo("C1");
    }
}