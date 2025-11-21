package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.AssignmentServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.AssignmentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private AssignmentRepository repository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private AssignmentMapper mapper = Mappers.getMapper(AssignmentMapper.class);

    @InjectMocks
    private AssignmentServiceImpl service;

    @Test
    void shouldCreateAssignmentSuccessfully() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var driver = User.builder().id(2L).name("Juan Driver").role(UserRole.DRIVER).build();
        var dispatcher = User.builder().id(3L).name("Pedro Dispatcher").role(UserRole.DISPATCHER).build();

        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(repository.existsByTripId(1L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(userRepository.findById(3L)).thenReturn(Optional.of(dispatcher));
        when(repository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.driverId()).isEqualTo(2L);
        assertThat(response.dispatcherId()).isEqualTo(3L);
        assertThat(response.checkListOk()).isTrue();
        assertThat(response.assignedAt()).isNotNull();

        verify(tripRepository).findById(1L);
        verify(repository).existsByTripId(1L);
        verify(userRepository).findById(2L);
        verify(userRepository).findById(3L);
        verify(repository).save(any(Assignment.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExists() {
        // Given
        var request = new AssignmentCreateRequest(99L, 2L, 3L, true);
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(tripRepository).findById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenTripAlreadyHasAssignment() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(repository.existsByTripId(1L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trip 1 already has an assignment");

        verify(tripRepository).findById(1L);
        verify(repository).existsByTripId(1L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDriverIsNotDriver() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var notDriver = User.builder().id(2L).name("Juan Passenger").role(UserRole.PASSENGER).build();

        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(repository.existsByTripId(1L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(notDriver));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User 2 is not a DRIVER");

        verify(tripRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDispatcherIsNotDispatcher() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var driver = User.builder().id(2L).role(UserRole.DRIVER).build();
        var notDispatcher = User.builder().id(3L).name("Ana Admin").role(UserRole.ADMIN).build();

        var request = new AssignmentCreateRequest(1L, 2L, 3L, true);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(repository.existsByTripId(1L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(userRepository.findById(3L)).thenReturn(Optional.of(notDispatcher));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User 3 is not a DISPATCHER");

        verify(tripRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(userRepository).findById(3L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateAssignmentSuccessfully() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var oldDriver = User.builder().id(2L).role(UserRole.DRIVER).build();
        var oldDispatcher = User.builder().id(3L).role(UserRole.DISPATCHER).build();
        var newDriver = User.builder().id(4L).role(UserRole.DRIVER).build();

        var existingAssignment = Assignment.builder()
                .id(10L)
                .trip(trip)
                .driver(oldDriver)
                .dispatcher(oldDispatcher)
                .checkListOk(false)
                .assignedAt(OffsetDateTime.now())
                .build();

        var updateRequest = new AssignmentUpdateRequest(4L, null, true);

        when(repository.findById(10L)).thenReturn(Optional.of(existingAssignment));
        when(userRepository.findById(4L)).thenReturn(Optional.of(newDriver));
        when(repository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.driverId()).isEqualTo(4L);
        assertThat(response.dispatcherId()).isEqualTo(3L);
        assertThat(response.checkListOk()).isTrue();

        verify(repository).findById(10L);
        verify(userRepository).findById(4L);
        verify(repository).save(any(Assignment.class));
    }

    @Test
    void shouldGetAssignmentById() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var driver = User.builder().id(2L).build();
        var dispatcher = User.builder().id(3L).build();

        var assignment = Assignment.builder()
                .id(10L)
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(assignment));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.driverId()).isEqualTo(2L);
        assertThat(response.dispatcherId()).isEqualTo(3L);

        verify(repository).findById(10L);
    }

    @Test
    void shouldDeleteAssignment() {
        // Given
        var assignment = Assignment.builder().id(10L).build();
        when(repository.findById(10L)).thenReturn(Optional.of(assignment));

        // When
        service.delete(10L);

        // Then
        verify(repository).findById(10L);
        verify(repository).delete(assignment);
    }

    @Test
    void shouldApproveChecklist() {
        // Given
        var assignment = Assignment.builder()
                .id(10L)
                .checkListOk(false)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(assignment));
        when(repository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.approveChecklist(10L);

        // Then
        assertThat(response.checkListOk()).isTrue();

        verify(repository).findById(10L);
        verify(repository).save(any(Assignment.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenChecklistAlreadyApproved() {
        // Given
        var assignment = Assignment.builder()
                .id(10L)
                .checkListOk(true)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(assignment));

        // When / Then
        assertThatThrownBy(() -> service.approveChecklist(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Checklist is already approved");

        verify(repository).findById(10L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldGetAssignmentsByTripId() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var driver = User.builder().id(2L).build();
        var dispatcher = User.builder().id(3L).build();

        var assignment = Assignment.builder()
                .id(10L)
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(assignment));

        when(repository.findByTripId(1L, pageable)).thenReturn(page);

        // When
        var result = service.getByTripId(1L, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).tripId()).isEqualTo(1L);

        verify(repository).findByTripId(1L, pageable);
    }

    @Test
    void shouldGetAssignmentsByDriverId() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var driver = User.builder().id(2L).build();
        var dispatcher = User.builder().id(3L).build();

        var assignment = Assignment.builder()
                .id(10L)
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(true)
                .assignedAt(OffsetDateTime.now())
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(assignment));

        when(repository.findByDriverId(2L, pageable)).thenReturn(page);

        // When
        var result = service.getByDriverId(2L, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).driverId()).isEqualTo(2L);

        verify(repository).findByDriverId(2L, pageable);
    }
}

