package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripResponse;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    // ========================= CREATE TESTS =========================
    @Test
    void shouldCreateTripSuccessfully() {
        // Given
        var route = Route.builder()
                .id(1L)
                .origin("Bogotá")
                .destination("Medellín")
                .build();

        var bus = Bus.builder()
                .id(2L)
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();

        var departureTime = OffsetDateTime.of(2025, 12, 20, 8, 0, 0, 0, ZoneOffset.UTC);
        var arrivalTime = OffsetDateTime.of(2025, 12, 20, 16, 0, 0, 0, ZoneOffset.UTC);

        var request = new TripCreateRequest(
                1L,
                2L,
                LocalDate.of(2025, 12, 20),
                departureTime,
                arrivalTime
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(2L)).thenReturn(Optional.of(bus));
        // CAMBIO: Usar new ArrayList<>() en lugar de List.of()
        when(tripRepository.findConflictingTrips(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>()); // Lista mutable vacía
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.busId()).isEqualTo(2L);
        assertThat(response.localDate()).isEqualTo(LocalDate.of(2025, 12, 20));
        assertThat(response.status()).isEqualTo(TripStatus.SCHEDULED);

        verify(routeRepository).findById(1L);
        verify(busRepository).findById(2L);
        verify(tripRepository).findConflictingTrips(any(), any(), any(), any());
        verify(tripRepository).save(any(Trip.class));
    }

    // ========================= UPDATE TESTS =========================

    @Test
    void shouldUpdateTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").capacity(40).status(true).build();
        var newRoute = Route.builder().id(3L).origin("Cali").destination("Cartagena").build();

        var existingTrip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .departureTime(OffsetDateTime.of(2025, 12, 20, 8, 0, 0, 0, ZoneOffset.UTC))
                .arrivalTime(OffsetDateTime.of(2025, 12, 20, 16, 0, 0, 0, ZoneOffset.UTC))
                .status(TripStatus.SCHEDULED)
                .build();

        var updateRequest = new TripUpdateRequest(
                3L,
                null,
                LocalDate.of(2025, 12, 21),
                null,
                null,
                null
        );

        when(tripRepository.findById(10L)).thenReturn(Optional.of(existingTrip));
        when(routeRepository.findById(3L)).thenReturn(Optional.of(newRoute));
        // CAMBIO: Usar new ArrayList<>() en lugar de List.of()
        when(tripRepository.findConflictingTrips(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>()); // Lista mutable vacía
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(3L);
        assertThat(response.localDate()).isEqualTo(LocalDate.of(2025, 12, 21));

        verify(tripRepository).findById(10L);
        verify(routeRepository).findById(3L);
        verify(tripRepository).save(any(Trip.class));
    }

    // ========================= GET TESTS =========================

    @Test
    void shouldGetTripById() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").capacity(40).status(true).build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .departureTime(OffsetDateTime.of(2025, 12, 20, 8, 0, 0, 0, ZoneOffset.UTC))
                .arrivalTime(OffsetDateTime.of(2025, 12, 20, 16, 0, 0, 0, ZoneOffset.UTC))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.busId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo(TripStatus.SCHEDULED);

        verify(tripRepository).findById(10L);
    }

    // ========================= DELETE TESTS =========================

    @Test
    void shouldDeleteTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findByTripId(10L)).thenReturn(List.of());
        doNothing().when(tripRepository).delete(trip);

        // When
        service.delete(10L);

        // Then
        verify(tripRepository).findById(10L);
        verify(tripRepository).delete(trip);
    }

    // ========================= QUERY TESTS =========================

    @Test
    void shouldFindTripsByStatus() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip1 = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        var trip2 = Trip.builder()
                .id(11L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 21))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findByStatus(TripStatus.SCHEDULED))
                .thenReturn(List.of(trip1, trip2));

        // When
        var result = service.findByStatus(TripStatus.SCHEDULED);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(1).id()).isEqualTo(11L);
        assertThat(result.get(0).status()).isEqualTo(TripStatus.SCHEDULED);

        verify(tripRepository).findByStatus(TripStatus.SCHEDULED);
    }

    @Test
    void shouldFindTripsByRouteId() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip1 = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findByRouteId(1L)).thenReturn(List.of(trip1));

        // When
        var result = service.findByRouteId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).routeId()).isEqualTo(1L);

        verify(tripRepository).findByRouteId(1L);
    }

    @Test
    void shouldFindTripsByBusId() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip1 = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findByBusId(2L)).thenReturn(List.of(trip1));

        // When
        var result = service.findByBusId(2L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).busId()).isEqualTo(2L);

        verify(tripRepository).findByBusId(2L);
    }

    @Test
    void shouldFindTripsByStatusAndBusId() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip1 = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findByStatusAndBusId(TripStatus.SCHEDULED, 2L))
                .thenReturn(List.of(trip1));

        // When
        var result = service.findByStatusAndBusId(TripStatus.SCHEDULED, 2L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(TripStatus.SCHEDULED);
        assertThat(result.get(0).busId()).isEqualTo(2L);

        verify(tripRepository).findByStatusAndBusId(TripStatus.SCHEDULED, 2L);
    }

    // ========================= BÚSQUEDA AVANZADA TESTS =========================

    @Test
    void shouldSearchAvailableTrips() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").capacity(40).build();

        var trip1 = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .departureTime(OffsetDateTime.of(2025, 12, 20, 8, 0, 0, 0, ZoneOffset.UTC))
                .arrivalTime(OffsetDateTime.of(2025, 12, 20, 16, 0, 0, 0, ZoneOffset.UTC))
                .status(TripStatus.SCHEDULED)
                .build();

        var trip2 = Trip.builder()
                .id(11L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .departureTime(OffsetDateTime.of(2025, 12, 20, 14, 0, 0, 0, ZoneOffset.UTC))
                .arrivalTime(OffsetDateTime.of(2025, 12, 20, 22, 0, 0, 0, ZoneOffset.UTC))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findAvailableTrips(eq(1L), eq(LocalDate.of(2025, 12, 20)), any()))
                .thenReturn(List.of(trip1, trip2));

        // When
        var result = service.searchAvailableTrips(1L, LocalDate.of(2025, 12, 20));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(1).id()).isEqualTo(11L);
        assertThat(result.get(0).status()).isEqualTo(TripStatus.SCHEDULED);

        verify(tripRepository).findAvailableTrips(eq(1L), eq(LocalDate.of(2025, 12, 20)), any());
    }

    @Test
    void shouldSearchTripsByRouteAndDate() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip1 = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findByRouteIdAndDate(1L, LocalDate.of(2025, 12, 20)))
                .thenReturn(List.of(trip1));

        // When
        var result = service.searchTrips(1L, LocalDate.of(2025, 12, 20), null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).routeId()).isEqualTo(1L);
        assertThat(result.get(0).localDate()).isEqualTo(LocalDate.of(2025, 12, 20));

        verify(tripRepository).findByRouteIdAndDate(1L, LocalDate.of(2025, 12, 20));
    }

    @Test
    void shouldSearchTripsByRouteAndDateAndStatus() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip1 = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findByRouteIdAndDateAndStatus(1L, LocalDate.of(2025, 12, 20), TripStatus.SCHEDULED))
                .thenReturn(List.of(trip1));

        // When
        var result = service.searchTrips(1L, LocalDate.of(2025, 12, 20), TripStatus.SCHEDULED);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(TripStatus.SCHEDULED);

        verify(tripRepository).findByRouteIdAndDateAndStatus(1L, LocalDate.of(2025, 12, 20), TripStatus.SCHEDULED);
    }

    // ========================= GESTIÓN DE ESTADOS TESTS =========================

    @Test
    void shouldBoardTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.boardTrip(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TripStatus.BOARDING);

        verify(tripRepository).findById(10L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldDepartTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();
        var driver = User.builder().id(3L).name("Juan Conductor").build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.BOARDING)
                .build();

        var assignment = Assignment.builder()
                .id(1L)
                .trip(trip)
                .driver(driver)
                .checkListOk(true)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(assignmentRepository.findFirstByTripId(10L)).thenReturn(Optional.of(assignment));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.departTrip(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TripStatus.DEPARTED);

        verify(tripRepository).findById(10L);
        verify(assignmentRepository).findFirstByTripId(10L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldArriveTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.DEPARTED)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.arriveTrip(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TripStatus.ARRIVED);

        verify(tripRepository).findById(10L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldCancelTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findByTripId(10L)).thenReturn(List.of());
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.cancelTrip(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TripStatus.CANCELLED);

        verify(tripRepository).findById(10L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void shouldRescheduleTripSuccessfully() {
        // Given
        var route = Route.builder().id(1L).origin("Bogotá").destination("Medellín").build();
        var bus = Bus.builder().id(2L).plate("ABC123").build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.CANCELLED)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.rescheduleTrip(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TripStatus.SCHEDULED);

        verify(tripRepository).findById(10L);
        verify(tripRepository).save(any(Trip.class));
    }

    // ========================= DISPONIBILIDAD TESTS =========================

    @Test
    void shouldGetAvailableSeatsCount() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).plate("ABC123").capacity(40).build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        var ticket1 = Ticket.builder()
                .id(1L)
                .trip(trip)
                .seatNumber("A1")
                .status(TicketStatus.SOLD)
                .build();

        var ticket2 = Ticket.builder()
                .id(2L)
                .trip(trip)
                .seatNumber("A2")
                .status(TicketStatus.SOLD)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(ticketRepository.findByTripId(10L)).thenReturn(List.of(ticket1, ticket2));

        // When
        var availableSeats = service.getAvailableSeatsCount(10L);

        // Then
        assertThat(availableSeats).isEqualTo(38); // 40 - 2 = 38

        verify(tripRepository).findById(10L);
        verify(ticketRepository).findByTripId(10L);
    }

    @Test
    void shouldGetOccupiedSeatsCount() {
        // Given
        var route = Route.builder().id(1L).build();
        var bus = Bus.builder().id(2L).plate("ABC123").capacity(40).build();

        var trip = Trip.builder()
                .id(10L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .status(TripStatus.SCHEDULED)
                .build();

        var ticket1 = Ticket.builder()
                .id(1L)
                .trip(trip)
                .seatNumber("A1")
                .status(TicketStatus.SOLD)
                .build();

        var ticket2 = Ticket.builder()
                .id(2L)
                .trip(trip)
                .seatNumber("A2")
                .status(TicketStatus.SOLD)
                .build();

        var ticket3 = Ticket.builder()
                .id(3L)
                .trip(trip)
                .seatNumber("A3")
                .status(TicketStatus.CANCELLED)
                .build();

        when(ticketRepository.findByTripId(10L)).thenReturn(List.of(ticket1, ticket2, ticket3));

        // When
        var occupiedSeats = service.getOccupiedSeatsCount(10L);

        // Then
        assertThat(occupiedSeats).isEqualTo(2); // Solo cuenta SOLD

        verify(ticketRepository).findByTripId(10L);
    }

    @Test
    void shouldCheckBusIsAvailable() {
        // Given
        when(tripRepository.findActiveTripsByBusAndDate(2L, LocalDate.of(2025, 12, 20)))
                .thenReturn(List.of());

        // When
        var isAvailable = service.isBusAvailable(2L, LocalDate.of(2025, 12, 20));

        // Then
        assertThat(isAvailable).isTrue();

        verify(tripRepository).findActiveTripsByBusAndDate(2L, LocalDate.of(2025, 12, 20));
    }

    // ========================= VALIDACIONES TESTS =========================

    @Test
    void shouldReturnTrueWhenTripCanBeDeleted() {
        // Given
        when(ticketRepository.findByTripId(10L)).thenReturn(List.of());

        // When
        var canBeDeleted = service.canBeDeleted(10L);

        // Then
        assertThat(canBeDeleted).isTrue();

        verify(ticketRepository).findByTripId(10L);
    }

    @Test
    void shouldReturnFalseWhenTripHasTicketsSold() {
        // Given
        var ticket = Ticket.builder()
                .id(1L)
                .seatNumber("A1")
                .status(TicketStatus.SOLD)
                .build();

        when(ticketRepository.findByTripId(10L)).thenReturn(List.of(ticket));

        // When
        var hasTicketsSold = service.hasTicketsSold(10L);

        // Then
        assertThat(hasTicketsSold).isTrue();

        verify(ticketRepository).findByTripId(10L);
    }

    @Test
    void shouldReturnFalseWhenTripHasNoTicketsSold() {
        // Given
        var ticket = Ticket.builder()
                .id(1L)
                .seatNumber("A1")
                .status(TicketStatus.CANCELLED)
                .build();

        when(ticketRepository.findByTripId(10L)).thenReturn(List.of(ticket));

        // When
        var hasTicketsSold = service.hasTicketsSold(10L);

        // Then
        assertThat(hasTicketsSold).isFalse();

        verify(ticketRepository).findByTripId(10L);
    }
}

