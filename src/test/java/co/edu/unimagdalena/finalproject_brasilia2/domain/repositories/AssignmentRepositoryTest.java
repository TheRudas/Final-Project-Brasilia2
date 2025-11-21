package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AssignmentRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private RouteRepository routeRepository;

    private User driver1;
    private User driver2;
    private User dispatcher1;
    private Trip trip1;
    private Trip trip2;
    private Trip trip3;
    private Assignment assignment1;
    private Assignment assignment2;
    private Assignment assignment3;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        busRepository.deleteAll();
        routeRepository.deleteAll();

        // Create drivers
        driver1 = User.builder()
                .name("Juan Perez")
                .email("juan.perez@bus.com")
                .phone("3001234567")
                .passwordHash("hashed_password_123")
                .role(UserRole.DRIVER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        driver1 = userRepository.save(driver1);

        driver2 = User.builder()
                .name("Maria Garcia")
                .email("maria.garcia@bus.com")
                .phone("3107654321")
                .passwordHash("hashed_password_456")
                .role(UserRole.DRIVER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        driver2 = userRepository.save(driver2);

        // Create dispatcher
        dispatcher1 = User.builder()
                .name("Carlos Admin")
                .email("carlos.admin@bus.com")
                .phone("3009876543")
                .passwordHash("hashed_admin_123")
                .role(UserRole.DISPATCHER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        dispatcher1 = userRepository.save(dispatcher1);

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

        // Create trips - cada uno único
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

        trip3 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(3))
                .departureTime(OffsetDateTime.now().plusDays(3))
                .arrivalTime(OffsetDateTime.now().plusDays(3).plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();
        trip3 = tripRepository.save(trip3);

        // Create assignments - un assignment por trip
        assignment1 = Assignment.builder()
                .trip(trip1)
                .driver(driver1)
                .dispatcher(dispatcher1)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build();

        assignment2 = Assignment.builder()
                .trip(trip2)
                .driver(driver1)
                .dispatcher(dispatcher1)
                .checkListOk(false)
                .assignedAt(OffsetDateTime.now())
                .build();

        assignment3 = Assignment.builder()
                .trip(trip3)
                .driver(driver2)
                .dispatcher(dispatcher1)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Assignment: find by driver id with pagination")
    void shouldFindByDriverId() {
        // Given
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);

        // When
        var result = assignmentRepository.findByDriverId(driver1.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Assignment::getDriver)
                .extracting(User::getName)
                .containsOnly("Juan Perez");
    }

    @Test
    @DisplayName("Assignment: find by trip id with pagination")
    void shouldFindByTripId() {
        // Given
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);

        // When
        var result = assignmentRepository.findByTripId(trip1.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTrip().getId()).isEqualTo(trip1.getId());
    }

    @Test
    @DisplayName("Assignment: find by id")
    void shouldFindById() {
        // Given
        assignment1 = assignmentRepository.save(assignment1);

        // When
        var result = assignmentRepository.findById(assignment1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDriver().getName()).isEqualTo("Juan Perez");
        assertThat(result.get().isCheckListOk()).isTrue();
    }

    @Test
    @DisplayName("Assignment: count by driver id")
    void shouldCountByDriverId() {
        // Given
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        assignmentRepository.save(assignment3);

        // When
        var countDriver1 = assignmentRepository.countByDriverId(driver1.getId());
        var countDriver2 = assignmentRepository.countByDriverId(driver2.getId());

        // Then
        assertThat(countDriver1).isEqualTo(2L);
        assertThat(countDriver2).isEqualTo(1L);
    }

    @Test
    @DisplayName("Assignment: count by checklist ok")
    void shouldCountByCheckListOk() {
        // Given
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        assignmentRepository.save(assignment3);

        // When
        var countOk = assignmentRepository.countByCheckListOk(true);
        var countNotOk = assignmentRepository.countByCheckListOk(false);

        // Then
        assertThat(countOk).isEqualTo(2L);
        assertThat(countNotOk).isEqualTo(1L);
    }

    @Test
    @DisplayName("Assignment: check if exists by driver id")
    void shouldCheckExistsByDriverId() {
        // Given
        assignmentRepository.save(assignment1);

        // When
        var exists = assignmentRepository.existsByDriverId(driver1.getId());
        var notExists = assignmentRepository.existsByDriverId(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Assignment: check if exists by trip id")
    void shouldCheckExistsByTripId() {
        // Given
        assignmentRepository.save(assignment1);

        // When
        var exists = assignmentRepository.existsByTripId(trip1.getId());
        var notExists = assignmentRepository.existsByTripId(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Assignment: find first by trip id")
    void shouldFindFirstByTripId() {
        // Given
        assignmentRepository.save(assignment1);

        // When
        var result = assignmentRepository.findFirstByTripId(trip1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTrip().getId()).isEqualTo(trip1.getId());
    }

    @Test
    @DisplayName("Assignment: return empty when driver has no assignments")
    void shouldReturnEmptyWhenDriverHasNoAssignments() {
        // Given - no assignments saved for driver2

        // When
        var result = assignmentRepository.findByDriverId(driver2.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Assignment: return empty when trip id not found")
    void shouldReturnEmptyWhenTripIdNotFound() {
        // Given
        assignmentRepository.save(assignment1);

        // When
        var result = assignmentRepository.findFirstByTripId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Assignment: return empty when id not found")
    void shouldReturnEmptyWhenIdNotFound() {
        // Given
        assignmentRepository.save(assignment1);

        // When
        var result = assignmentRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Assignment: verify dispatcher relationship")
    void shouldVerifyDispatcherRelationship() {
        // Given
        assignmentRepository.save(assignment1);

        // When
        var result = assignmentRepository.findById(assignment1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDispatcher()).isNotNull();
        assertThat(result.get().getDispatcher().getName()).isEqualTo("Carlos Admin");
        assertThat(result.get().getDispatcher().getRole()).isEqualTo(UserRole.DISPATCHER);
    }

    @Test
    @DisplayName("Assignment: return zero count when driver has no assignments")
    void shouldReturnZeroCountWhenDriverHasNoAssignments() {
        // Given - no assignments for driver2

        // When
        var count = assignmentRepository.countByDriverId(driver2.getId());

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("Assignment: return zero count when no checklist with specific status")
    void shouldReturnZeroCountWhenNoChecklistWithStatus() {
        // Given
        assignmentRepository.save(assignment1); // checkListOk = true

        // When
        var count = assignmentRepository.countByCheckListOk(false);

        // Then
        assertThat(count).isEqualTo(0L);
    }
}