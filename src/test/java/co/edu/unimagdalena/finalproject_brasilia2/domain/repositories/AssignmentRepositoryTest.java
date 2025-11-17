package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;


class AssignmentRepositoryTest extends AbstractRepositoryIT {
    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private RouteRepository routeRepository;

    private User driver1;
    private User driver2;
    private User dispatcher;
    private Trip trip1;
    private Trip trip2;
    private Trip trip3;
    private Bus bus;
    private Route route;
    private Assignment assignment1;
    private Assignment assignment2;
    private Assignment assignment3;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        tripRepository.deleteAll();
        busRepository.deleteAll();
        routeRepository.deleteAll();
        userRepository.deleteAll();

        baseTime = OffsetDateTime.now();

        // Crear conductores
        driver1 = User.builder()
                .name("Juan Pérez")
                .email("juan@example.com")
                .phone("3001234567")
                .role(UserRole.DRIVER)
                .status(true)
                .passwordHash("hash123")
                .createdAt(baseTime)
                .build();

        driver2 = User.builder()
                .name("María García")
                .email("maria@example.com")
                .phone("3007654321")
                .role(UserRole.DRIVER)
                .status(true)
                .passwordHash("hash456")
                .createdAt(baseTime)
                .build();

        // Crear despachador
        dispatcher = User.builder()
                .name("Carlos Admin")
                .email("carlos@example.com")
                .phone("3009876543")
                .role(UserRole.DISPATCHER)
                .status(true)
                .passwordHash("hash789")
                .createdAt(baseTime)
                .build();

        userRepository.save(driver1);
        userRepository.save(driver2);
        userRepository.save(dispatcher);

        // Crear bus
        bus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();

        busRepository.save(bus);

        // Crear ruta
        route = Route.builder()
                .code("R001")
                .name("Ruta Norte")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(420)
                .build();

        routeRepository.save(route);

        // Crear viajes
        trip1 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(baseTime.plusDays(1))
                .arrivalTime(baseTime.plusHours(1).plusHours(7))
                .status(TripStatus.SCHEDULED)
                .build();

        trip2 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(2))
                .departureTime(baseTime.plusDays(2))
                .arrivalTime(baseTime.plusHours(2).plusHours(7))
                .status(TripStatus.SCHEDULED)
                .build();

        trip3 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(3))
                .departureTime(baseTime.plusDays(3))
                .arrivalTime(baseTime.plusDays(3).plusHours(7))
                .status(TripStatus.SCHEDULED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);

        // Crear asignaciones
        assignment1 = Assignment.builder()
                .trip(trip1)
                .driver(driver1)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(baseTime.minusHours(5))
                .build();

        assignment2 = Assignment.builder()
                .trip(trip2)
                .driver(driver1)
                .dispatcher(dispatcher)
                .checkListOk(false)
                .assignedAt(baseTime.minusHours(3))
                .build();

        assignment3 = Assignment.builder()
                .trip(trip3)
                .driver(driver2)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(baseTime.minusHours(2))
                .build();
    }
    @Test
    @DisplayName("Assignment: find by driver")
    void shouldFindByDriverID() {
        // Given
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        assignmentRepository.save(assignment3);

        // When
        var driver1Assignments = assignmentRepository.findByDriverId(driver1.getId());
        var driver2Assignments = assignmentRepository.findByDriverId(driver2.getId());

        // Then
        assertThat(driver1Assignments).hasSize(2);
        assertThat(driver2Assignments).hasSize(1);
    }

    @Test
    @DisplayName("Assignment: find by Trip id")
    void shouldFindByTripID() {
        // Given
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        assignmentRepository.save(assignment3);
        // When
        var assignmentForTrip1 = assignmentRepository.findByTripId(trip1.getId());
        // Then
        assertThat(assignmentForTrip1).hasSize(1);
        assertThat(assignmentForTrip1.get(0).getDriver().getName()).isEqualTo("Juan Pérez");
        assertThat(assignmentForTrip1.get(0).isCheckListOk()).isTrue();
    }


    @Test
    @DisplayName("Assignment: count by driver id")
    void shouldCountByDriverId() {
        // Given
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        assignmentRepository.save(assignment3);

        // When
        var driver1Count = assignmentRepository.countByDriverId(driver1.getId());
        var driver2Count = assignmentRepository.countByDriverId(driver2.getId());

        // Then
        assertThat(driver1Count).isEqualTo(2);
        assertThat(driver2Count).isEqualTo(1);
    }
    @Test
    @DisplayName("Assignment: count by checklist OK status")
    void shouldCountByCheckListOk() {
        // Given
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        assignmentRepository.save(assignment3);

        // When
        var completedCount = assignmentRepository.countByCheckListOk(true);
        var pendingCount = assignmentRepository.countByCheckListOk(false);

        // Then
        assertThat(completedCount).isEqualTo(2);
        assertThat(pendingCount).isEqualTo(1);
    }


    @Test
    @DisplayName("Assignment: exists by driver id")
    void shouldExistsByDriverId() {
        // Given
        assignmentRepository.save(assignment1);

        // When
        var exists = assignmentRepository.existsByDriverId(driver1.getId());
        var notExists = assignmentRepository.existsByDriverId(driver2.getId());

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}