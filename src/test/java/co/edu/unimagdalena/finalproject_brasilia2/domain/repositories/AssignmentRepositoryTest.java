package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Assignment;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentRepositoryTest extends AbstractRepositoryIT {

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
    private User dispatcher;
    private Trip trip1;
    private Trip trip2;
    private Assignment assignment1;
    private Assignment assignment2;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        busRepository.deleteAll();
        routeRepository.deleteAll();

        // Create drivers
        driver1 = userRepository.save(User.builder()
                .name("Driver One")
                .email("driver1@test.com")
                .phone("3001234567")
                .role(UserRole.DRIVER)
                .status(true)
                .passwordHash("hash123")
                .createdAt(OffsetDateTime.now())
                .build());

        driver2 = userRepository.save(User.builder()
                .name("Driver Two")
                .email("driver2@test.com")
                .phone("3001234568")
                .role(UserRole.DRIVER)
                .status(true)
                .passwordHash("hash456")
                .createdAt(OffsetDateTime.now())
                .build());

        // Create dispatcher
        dispatcher = userRepository.save(User.builder()
                .name("Dispatcher")
                .email("dispatcher@test.com")
                .phone("3009876543")
                .role(UserRole.DISPATCHER)
                .status(true)
                .passwordHash("hash789")
                .createdAt(OffsetDateTime.now())
                .build());

        // Create bus
        Bus bus = busRepository.save(Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build());

        // Create route
        Route route = routeRepository.save(Route.builder()
                .code("R001")
                .name("Route 1")
                .origin("City A")
                .destination("City B")
                .distanceKm(new BigDecimal("100"))
                .durationMin(120)
                .build());

        // Create trips
        trip1 = tripRepository.save(Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusHours(4))
                .status(TripStatus.SCHEDULED)
                .build());

        trip2 = tripRepository.save(Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(OffsetDateTime.now().plusDays(1).plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusDays(1).plusHours(4))
                .status(TripStatus.SCHEDULED)
                .build());

        // Create assignments
        assignment1 = assignmentRepository.save(Assignment.builder()
                .trip(trip1)
                .driver(driver1)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build());

        assignment2 = assignmentRepository.save(Assignment.builder()
                .trip(trip2)
                .driver(driver2)
                .dispatcher(dispatcher)
                .checkListOk(false)
                .assignedAt(OffsetDateTime.now())
                .build());
    }

    @Test
    void testFindByDriverId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Assignment> driver1Assignments = assignmentRepository.findByDriverId(driver1.getId(), pageable);
        Page<Assignment> driver2Assignments = assignmentRepository.findByDriverId(driver2.getId(), pageable);

        // Assert
        assertThat(driver1Assignments).isNotNull();
        assertThat(driver1Assignments.getContent()).hasSize(1);
        assertThat(driver1Assignments.getContent().get(0).getDriver().getName()).isEqualTo("Driver One");

        assertThat(driver2Assignments).isNotNull();
        assertThat(driver2Assignments.getContent()).hasSize(1);
        assertThat(driver2Assignments.getContent().get(0).getDriver().getName()).isEqualTo("Driver Two");
    }

    @Test
    void testFindByDriverId_WithPagination() {
        // Arrange - Create multiple assignments for driver1
        Trip trip3 = tripRepository.save(Trip.builder()
                .route(trip1.getRoute())
                .bus(trip1.getBus())
                .date(LocalDate.now().plusDays(2))
                .departureTime(OffsetDateTime.now().plusDays(2).plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusDays(2).plusHours(4))
                .status(TripStatus.SCHEDULED)
                .build());

        assignmentRepository.save(Assignment.builder()
                .trip(trip3)
                .driver(driver1)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build());

        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Assignment> page1 = assignmentRepository.findByDriverId(driver1.getId(), pageable);
        Page<Assignment> page2 = assignmentRepository.findByDriverId(driver1.getId(), PageRequest.of(1, 1));

        // Assert
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page1.getTotalElements()).isEqualTo(2);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.hasNext()).isTrue();
        assertThat(page2.hasNext()).isFalse();
    }

    @Test
    void testFindByTripId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Assignment> trip1Assignments = assignmentRepository.findByTripId(trip1.getId(), pageable);

        // Assert
        assertThat(trip1Assignments).isNotNull();
        assertThat(trip1Assignments.getContent()).hasSize(1);
        assertThat(trip1Assignments.getContent().get(0).getTrip().getId()).isEqualTo(trip1.getId());
        assertThat(trip1Assignments.getContent().get(0).getDriver().getName()).isEqualTo("Driver One");
    }

    @Test
    void testFindById() {
        // Act
        Assignment found = assignmentRepository.findById(assignment1.getId()).orElse(null);

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(assignment1.getId());
        assertThat(found.getDriver().getName()).isEqualTo("Driver One");
        assertThat(found.isCheckListOk()).isTrue();
    }

    @Test
    void testCountByDriverId() {
        // Act
        long driver1Count = assignmentRepository.countByDriverId(driver1.getId());
        long driver2Count = assignmentRepository.countByDriverId(driver2.getId());

        // Assert
        assertThat(driver1Count).isEqualTo(1);
        assertThat(driver2Count).isEqualTo(1);
    }

    @Test
    void testCountByCheckListOk() {
        // Act
        long completedChecklists = assignmentRepository.countByCheckListOk(true);
        long pendingChecklists = assignmentRepository.countByCheckListOk(false);

        // Assert
        assertThat(completedChecklists).isEqualTo(1);
        assertThat(pendingChecklists).isEqualTo(1);
    }

    @Test
    void testExistsByDriverId() {
        // Act & Assert
        assertThat(assignmentRepository.existsByDriverId(driver1.getId())).isTrue();
        assertThat(assignmentRepository.existsByDriverId(driver2.getId())).isTrue();
        assertThat(assignmentRepository.existsByDriverId(999L)).isFalse();
    }

    @Test
    void testSaveAndRetrieve() {
        // Arrange
        Trip trip3 = tripRepository.save(Trip.builder()
                .route(trip1.getRoute())
                .bus(trip1.getBus())
                .date(LocalDate.now().plusDays(3))
                .departureTime(OffsetDateTime.now().plusDays(3).plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusDays(3).plusHours(4))
                .status(TripStatus.SCHEDULED)
                .build());

        Assignment newAssignment = Assignment.builder()
                .trip(trip3)
                .driver(driver1)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build();

        // Act
        Assignment saved = assignmentRepository.save(newAssignment);
        Assignment retrieved = assignmentRepository.findById(saved.getId()).orElse(null);

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getDriver().getId()).isEqualTo(driver1.getId());
        assertThat(retrieved.isCheckListOk()).isTrue();
    }

    @Test
    void testUpdate() {
        // Arrange
        assignment1.setCheckListOk(false);

        // Act
        assignmentRepository.save(assignment1);
        Assignment updated = assignmentRepository.findById(assignment1.getId()).orElse(null);

        // Assert
        assertThat(updated).isNotNull();
        assertThat(updated.isCheckListOk()).isFalse();
    }

    @Test
    void testDelete() {
        // Arrange
        Long assignmentId = assignment1.getId();

        // Act
        assignmentRepository.delete(assignment1);

        // Assert
        assertThat(assignmentRepository.findById(assignmentId)).isEmpty();
        assertThat(assignmentRepository.existsByDriverId(driver1.getId())).isFalse();
    }

    @Test
    void testFindByDriverId_EmptyResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Assignment> result = assignmentRepository.findByDriverId(999L, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testFindByTripId_EmptyResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Assignment> result = assignmentRepository.findByTripId(999L, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testMultipleAssignmentsSameDispatcher() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        long totalAssignments = assignmentRepository.count();
        Page<Assignment> driver1Assignments = assignmentRepository.findByDriverId(driver1.getId(), pageable);
        Page<Assignment> driver2Assignments = assignmentRepository.findByDriverId(driver2.getId(), pageable);

        // Assert
        assertThat(totalAssignments).isEqualTo(2);
        assertThat(driver1Assignments.getContent().get(0).getDispatcher().getId())
                .isEqualTo(driver2Assignments.getContent().get(0).getDispatcher().getId());
    }

    @Test
    void testCountByCheckListOk_WithMultipleAssignments() {
        // Arrange
        Trip trip3 = tripRepository.save(Trip.builder()
                .route(trip1.getRoute())
                .bus(trip1.getBus())
                .date(LocalDate.now().plusDays(4))
                .departureTime(OffsetDateTime.now().plusDays(4).plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusDays(4).plusHours(4))
                .status(TripStatus.SCHEDULED)
                .build());

        assignmentRepository.save(Assignment.builder()
                .trip(trip3)
                .driver(driver1)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build());

        // Act
        long completedChecklists = assignmentRepository.countByCheckListOk(true);
        long pendingChecklists = assignmentRepository.countByCheckListOk(false);

        // Assert
        assertThat(completedChecklists).isEqualTo(2);
        assertThat(pendingChecklists).isEqualTo(1);
    }
}