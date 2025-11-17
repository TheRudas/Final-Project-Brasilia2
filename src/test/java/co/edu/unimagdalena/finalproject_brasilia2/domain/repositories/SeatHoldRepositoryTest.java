package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SeatHoldRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private RouteRepository routeRepository;

    private User user1;
    private User user2;
    private Trip trip1;
    private Trip trip2;
    private SeatHold hold1;
    private SeatHold hold2;
    private SeatHold hold3;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        seatHoldRepository.deleteAll();
        tripRepository.deleteAll();
        busRepository.deleteAll();
        routeRepository.deleteAll();
        userRepository.deleteAll();

        baseTime = OffsetDateTime.now();

        user1 = User.builder()
                .name("User One")
                .email("user1@example.com")
                .phone("3001111111")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hash1")
                .createdAt(baseTime)
                .build();

        user2 = User.builder()
                .name("User Two")
                .email("user2@example.com")
                .phone("3002222222")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hash2")
                .createdAt(baseTime)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        Bus bus = Bus.builder()
                .plate("XYZ123")
                .capacity(40)
                .status(true)
                .build();
        busRepository.save(bus);

        Route route = Route.builder()
                .code("R002")
                .name("Ruta Sur")
                .origin("Santa Marta")
                .destination("Barranquilla")
                .distanceKm(new BigDecimal(5000))
                .durationMin(120)
                .build();
        routeRepository.save(route);

        trip1 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(baseTime.plusHours(3))
                .arrivalTime(baseTime.plusHours(5))
                .status(TripStatus.SCHEDULED)
                .build();

        trip2 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(baseTime.plusHours(6))
                .arrivalTime(baseTime.plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);

        hold1 = SeatHold.builder()
                .trip(trip1)
                .user(user1)
                .seatNumber("A1")
                .expiresAt(baseTime.plusMinutes(10))
                .status(SeatHoldStatus.HOLD)
                .build();

        hold2 = SeatHold.builder()
                .trip(trip1)
                .user(user2)
                .seatNumber("A2")
                .expiresAt(baseTime.plusMinutes(10))
                .status(SeatHoldStatus.HOLD)
                .build();

        hold3 = SeatHold.builder()
                .trip(trip2)
                .user(user1)
                .seatNumber("B1")
                .expiresAt(baseTime.plusMinutes(10))
                .status(SeatHoldStatus.EXPIRED)
                .build();
    }

    @Test
    @DisplayName("SeatHold: find by Trip ID")
    void shouldFindByTripId() {
        seatHoldRepository.save(hold1);
        seatHoldRepository.save(hold2);
        seatHoldRepository.save(hold3);

        var trip1Holds = seatHoldRepository.findByTripId(trip1.getId());
        var trip2Holds = seatHoldRepository.findByTripId(trip2.getId());

        assertThat(trip1Holds).hasSize(2);
        assertThat(trip2Holds).hasSize(1);
    }

    @Test
    @DisplayName("SeatHold: find by User ID")
    void shouldFindByUserId() {
        seatHoldRepository.save(hold1);
        seatHoldRepository.save(hold2);
        seatHoldRepository.save(hold3);

        var user1Holds = seatHoldRepository.findByUserId(user1.getId());
        var user2Holds = seatHoldRepository.findByUserId(user2.getId());

        assertThat(user1Holds).hasSize(2);
        assertThat(user2Holds).hasSize(1);
    }

    @Test
    @DisplayName("SeatHold: find by User ID and Trip ID")
    void shouldFindByUserIdAndTripId() {
        seatHoldRepository.save(hold1);
        seatHoldRepository.save(hold2);
        seatHoldRepository.save(hold3);

        var result = seatHoldRepository.findByUserIdAndTripId(user1.getId(), trip1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeatNumber()).isEqualTo("A1");
    }

    @Test
    @DisplayName("SeatHold: exists by User ID and Trip ID")
    void shouldCheckExistsByUserIdAndTripId() {
        seatHoldRepository.save(hold1);

        assertThat(seatHoldRepository.existsByUserIdAndTripId(user1.getId(), trip1.getId())).isTrue();
        assertThat(seatHoldRepository.existsByUserIdAndTripId(user2.getId(), trip1.getId())).isFalse();
    }

    @Test
    @DisplayName("SeatHold: exists by SeatHold ID")
    void shouldCheckExistsById() {
        SeatHold saved = seatHoldRepository.save(hold1);

        assertThat(seatHoldRepository.existsSeatHoldById(saved.getId())).isTrue();
        assertThat(seatHoldRepository.existsSeatHoldById(999L)).isFalse();
    }
}
