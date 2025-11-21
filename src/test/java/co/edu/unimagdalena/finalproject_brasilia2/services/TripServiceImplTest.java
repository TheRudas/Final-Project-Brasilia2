package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.TripServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.TripMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Spy
    private TripMapper mapper = Mappers.getMapper(TripMapper.class);

    @InjectMocks
    private TripServiceImpl service;

    @Test
    void shouldCreateTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).name("Bogotá-Tunja").build();
        var bus = Bus.builder().id(10L).plate("ABC123").status(true).capacity(40).build();

        var departureTime = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        var arrivalTime = departureTime.plusHours(3);

        var request = new TripCreateRequest(
                1L,
                10L,
                LocalDate.now().plusDays(1),
                departureTime,
                arrivalTime
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(10L)).thenReturn(Optional.of(bus));
        when(tripRepository.findConflictingTrips(any(), any(), any(), any())).thenReturn(List.of());
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.busId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TripStatus.SCHEDULED);

        verify(routeRepository).findById(1L);
        verify(busRepository).findById(10L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRouteNotExists() {
        // Given
        var departureTime = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        var arrivalTime = departureTime.plusHours(3);

        var request = new TripCreateRequest(
                99L,
                10L,
                LocalDate.now().plusDays(1),
                departureTime,
                arrivalTime
        );

        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 99 not found");

        verify(routeRepository).findById(99L);
        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenBusIsInactive() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).plate("ABC123").status(false).build();

        var departureTime = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        var arrivalTime = departureTime.plusHours(3);

        var request = new TripCreateRequest(
                1L,
                10L,
                LocalDate.now().plusDays(1),
                departureTime,
                arrivalTime
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(10L)).thenReturn(Optional.of(bus));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Bus ABC123 is not active/operational");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDateIsInPast() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).status(true).build();

        var departureTime = OffsetDateTime.now().minusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        var arrivalTime = departureTime.plusHours(3);

        var request = new TripCreateRequest(
                1L,
                10L,
                LocalDate.now().minusDays(1),
                departureTime,
                arrivalTime
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(10L)).thenReturn(Optional.of(bus));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trip date cannot be in the past");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenArrivalBeforeDeparture() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).status(true).build();

        var departureTime = OffsetDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0);
        var arrivalTime = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);

        var request = new TripCreateRequest(
                1L,
                10L,
                LocalDate.now().plusDays(1),
                departureTime,
                arrivalTime
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(10L)).thenReturn(Optional.of(bus));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arrival time must be after departure time");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).plate("ABC123").status(true).capacity(40).build();

        var departureTime = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        var arrivalTime = departureTime.plusHours(3);

        var existingTrip = Trip.builder()
                .id(100L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        var newDepartureTime = OffsetDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        var newArrivalTime = newDepartureTime.plusHours(3);

        var updateRequest = new TripUpdateRequest(
                null,
                null,
                null,
                newDepartureTime,
                newArrivalTime,
                null
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(100L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(100L);

        verify(tripRepository).findById(100L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUpdateCompletedTrip() {
        // Given
        var trip = Trip.builder()
                .id(100L)
                .status(TripStatus.ARRIVED)
                .build();

        var updateRequest = new TripUpdateRequest(
                null,
                null,
                LocalDate.now().plusDays(2),
                null,
                null,
                null
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));

        // When / Then
        assertThatThrownBy(() -> service.update(100L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update a trip with status ARRIVED");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldGetTripById() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).build();

        var departureTime = OffsetDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        var arrivalTime = departureTime.plusHours(3);

        var trip = Trip.builder()
                .id(100L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));

        // When
        var response = service.get(100L);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(TripStatus.SCHEDULED);

        verify(tripRepository).findById(100L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentTrip() {
        // Given
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(tripRepository).findById(99L);
    }

    @Test
    void shouldDeleteTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).build();

        var trip = Trip.builder()
                .id(100L)
                .route(route)
                .bus(bus)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findByTripId(100L)).thenReturn(List.of());
        doNothing().when(tripRepository).delete(trip);

        // When
        service.delete(100L);

        // Then
        verify(tripRepository).findById(100L);
        verify(tripRepository).delete(trip);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenDeleteTripWithTickets() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var ticket = Ticket.builder().id(1L).status(TicketStatus.SOLD).build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findByTripId(100L)).thenReturn(List.of(ticket));

        // When / Then
        assertThatThrownBy(() -> service.delete(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete trip 100: it has sold tickets");

        verify(tripRepository, never()).delete(any());
    }

    @Test
    void shouldBoardTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).plate("ABC123").build();

        var trip = Trip.builder()
                .id(100L)
                .route(route)
                .bus(bus)
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.boardTrip(100L);

        // Then
        assertThat(response.status()).isEqualTo(TripStatus.BOARDING);

        verify(tripRepository).findById(100L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenBoardNonScheduledTrip() {
        // Given
        var trip = Trip.builder()
                .id(100L)
                .status(TripStatus.DEPARTED)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));

        // When / Then
        assertThatThrownBy(() -> service.boardTrip(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only SCHEDULED trips can start boarding");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldDepartTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).plate("ABC123").build();
        var driver = User.builder().id(5L).name("Driver").build();

        var trip = Trip.builder()
                .id(100L)
                .route(route)
                .bus(bus)
                .status(TripStatus.BOARDING)
                .build();

        var assignment = Assignment.builder()
                .id(1L)
                .trip(trip)
                .driver(driver)
                .checkListOk(true)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.findFirstByTripId(100L)).thenReturn(Optional.of(assignment));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.departTrip(100L);

        // Then
        assertThat(response.status()).isEqualTo(TripStatus.DEPARTED);

        verify(tripRepository).findById(100L);
        verify(assignmentRepository).findFirstByTripId(100L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenDepartWithoutAssignment() {
        // Given
        var trip = Trip.builder()
                .id(100L)
                .status(TripStatus.BOARDING)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.findFirstByTripId(100L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.departTrip(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trip 100 must have a driver assigned before departure");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldArriveTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).plate("ABC123").build();

        var trip = Trip.builder()
                .id(100L)
                .route(route)
                .bus(bus)
                .status(TripStatus.DEPARTED)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.arriveTrip(100L);

        // Then
        assertThat(response.status()).isEqualTo(TripStatus.ARRIVED);

        verify(tripRepository).findById(100L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldCancelTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).plate("ABC123").build();

        var trip = Trip.builder()
                .id(100L)
                .route(route)
                .bus(bus)
                .status(TripStatus.SCHEDULED)
                .date(LocalDate.now().plusDays(1))
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findByTripId(100L)).thenReturn(List.of());
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.cancelTrip(100L);

        // Then
        assertThat(response.status()).isEqualTo(TripStatus.CANCELLED);

        verify(tripRepository).findById(100L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCancelTripWithSoldTickets() {
        // Given
        var trip = Trip.builder()
                .id(100L)
                .status(TripStatus.SCHEDULED)
                .build();

        var ticket = Ticket.builder().id(1L).status(TicketStatus.SOLD).build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findByTripId(100L)).thenReturn(List.of(ticket));

        // When / Then
        assertThatThrownBy(() -> service.cancelTrip(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel trip 100: it has sold tickets");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldFindTripsByRouteId() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(10L).build();

        var trip = Trip.builder()
                .id(100L)
                .route(route)
                .bus(bus)
                .build();

        when(tripRepository.findByRouteId(1L)).thenReturn(List.of(trip));

        // When
        var result = service.findByRouteId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().routeId()).isEqualTo(1L);

        verify(tripRepository).findByRouteId(1L);
    }

    @Test
    void shouldGetAvailableSeatsCount() {
        // Given
        var bus = Bus.builder().id(10L).capacity(40).build();
        var trip = Trip.builder().id(100L).bus(bus).build();

        var ticket1 = Ticket.builder().id(1L).seatNumber("A1").status(TicketStatus.SOLD).build();
        var ticket2 = Ticket.builder().id(2L).seatNumber("A2").status(TicketStatus.SOLD).build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findByTripId(100L)).thenReturn(List.of(ticket1, ticket2));

        // When
        var available = service.getAvailableSeatsCount(100L);

        // Then
        assertThat(available).isEqualTo(38); // 40 - 2

        verify(tripRepository).findById(100L);
        verify(ticketRepository).findByTripId(100L);
    }

    @Test
    void shouldGetOccupiedSeatsCount() {
        // Given
        var ticket1 = Ticket.builder().id(1L).seatNumber("A1").status(TicketStatus.SOLD).build();
        var ticket2 = Ticket.builder().id(2L).seatNumber("A2").status(TicketStatus.SOLD).build();
        var ticket3 = Ticket.builder().id(3L).seatNumber("A3").status(TicketStatus.CANCELLED).build();

        when(ticketRepository.findByTripId(100L)).thenReturn(List.of(ticket1, ticket2, ticket3));

        // When
        var occupied = service.getOccupiedSeatsCount(100L);

        // Then
        assertThat(occupied).isEqualTo(2); // Solo los SOLD

        verify(ticketRepository).findByTripId(100L);
    }
}

