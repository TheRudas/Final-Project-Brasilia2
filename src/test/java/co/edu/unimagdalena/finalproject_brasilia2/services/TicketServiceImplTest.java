package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos.TicketCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos.TicketUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.TicketServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.TicketMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StopRepository stopRepository;
    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ConfigService configService;

    @Mock
    private FareRuleService fareRuleService;

    @Mock
    private NotificationService notificationService;

    @Spy
    private TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @InjectMocks
    private TicketServiceImpl service;
    // Helper method to create test data
    private Route createTestRoute() {
        return Route.builder().id(1L).code("RUT-001").name("Bogota-Medellin").origin("Bogota").destination("Medellin").distanceKm(new BigDecimal("400.00"))
                .durationMin(360).build();
    }

    private Bus createTestBus() {
        return Bus.builder().id(1L).plate("ABC123").capacity(40).amenities(new HashSet<>()).status(true).build();
    }

    private Trip createTestTrip(Route route, Bus bus) {
        return Trip.builder().id(1L).route(route).bus(bus).date(LocalDate.now()).departureTime(OffsetDateTime.now().plusHours(2)).arrivalTime(OffsetDateTime.now()
                .plusHours(8)).status(TripStatus.SCHEDULED).build();
    }

    private User createTestPassenger() {
        return User.builder().id(1L).name("Juan Perez").email("juan.perez@example.com").phone("3001234567").role(UserRole.PASSENGER).status(true)
                .passwordHash("hashedPassword").createdAt(OffsetDateTime.now()).build();
    }

    private Stop createTestStop(Route route, String name, Integer order) {
        return Stop.builder().id(order.longValue()).route(route).name(name).order(order).build();
    }

    // ============= CREATE TESTS =============

    @Test
    void shouldCreateTicketSuccessfully() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var seat = Seat.builder()
                .id(1L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var request = new TicketCreateRequest(1L, 1L, "A1", 1L, 5L, new BigDecimal("50000.00"), PaymentMethod.CARD);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(seatHoldRepository.findByTripIdAndSeatNumberAndStatus(1L, "A1", SeatHoldStatus.HOLD))
                .thenReturn(Optional.empty()); // No hay hold activo
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(bus.getId(), "A1")).thenReturn(Optional.of(seat));
        when(ticketRepository.existsOverlappingTicket(1L, "A1", 1, 5)).thenReturn(false);
        doReturn(new BigDecimal("50000.00"))
                .when(fareRuleService).calculateTicketPrice(anyLong(), anyLong(), anyLong(), any());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(10L);
            t.setStatus(TicketStatus.SOLD);
            return t;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.passengerId()).isEqualTo(1L);
        assertThat(response.passengerName()).isEqualTo("Juan Perez");
        assertThat(response.busPlate()).isEqualTo("ABC123");
        assertThat(response.seatNumber()).isEqualTo("A1");
        assertThat(response.fromStopId()).isEqualTo(1L);
        assertThat(response.toStopId()).isEqualTo(5L);
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.qrCode()).startsWith("QR-");

        verify(tripRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(stopRepository).findById(1L);
        verify(stopRepository).findById(5L);
        verify(seatRepository).findByBusIdAndNumber(bus.getId(), "A1");
        verify(ticketRepository).existsOverlappingTicket(1L, "A1", 1, 5);
        verify(ticketRepository).save(any(Ticket.class));
        verify(notificationService).sendTicketConfirmation(
                eq("3001234567"),
                eq("Juan Perez"),
                eq(10L),
                eq("A1"),
                anyString(), // QR code
                eq("Bogota-Medellin")
        );
    }

    @Test
    void shouldCreateTicketAndExpireSeatHoldWhenHoldExists() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var seat = Seat.builder()
                .id(1L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        // Crear un SeatHold activo que debe ser expirado
        var seatHold = SeatHold.builder()
                .id(5L)
                .trip(trip)
                .user(passenger)
                .seatNumber("A1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .build();

        var request = new TicketCreateRequest(1L, 1L, "A1", 1L, 5L, new BigDecimal("50000.00"), PaymentMethod.CARD);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(bus.getId(), "A1")).thenReturn(Optional.of(seat));
        when(ticketRepository.existsOverlappingTicket(1L, "A1", 1, 5)).thenReturn(false);
        when(seatHoldRepository.findByTripIdAndSeatNumberAndStatus(1L, "A1", SeatHoldStatus.HOLD))
                .thenReturn(Optional.of(seatHold));
        doReturn(new BigDecimal("50000.00"))
                .when(fareRuleService).calculateTicketPrice(anyLong(), anyLong(), anyLong(), any());
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(10L);
            t.setStatus(TicketStatus.SOLD);
            return t;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A1");
        assertThat(response.status()).isEqualTo(TicketStatus.SOLD);

        // Verificar que el SeatHold fue marcado como EXPIRED
        verify(seatHoldRepository).findByTripIdAndSeatNumberAndStatus(1L, "A1", SeatHoldStatus.HOLD);
        verify(seatHoldRepository).save(argThat(hold ->
                hold.getId().equals(5L) && hold.getStatus() == SeatHoldStatus.EXPIRED
        ));
        verify(ticketRepository).save(any(Ticket.class));
    }


    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExists() {
        // Given
        var request = new TicketCreateRequest(
                99L, 1L, "A1", 1L, 5L,
                new BigDecimal("50000.00"), PaymentMethod.CARD
        );
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(tripRepository).findById(99L);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPassengerNotExists() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);

        var request = new TicketCreateRequest(
                1L, 99L, "A1", 1L, 5L,
                new BigDecimal("50000.00"), PaymentMethod.CARD
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Passenger 99 not found");

        verify(tripRepository).findById(1L);
        verify(userRepository).findById(99L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFromStopNotExists() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();

        var request = new TicketCreateRequest(
                1L, 1L, "A1", 99L, 5L,
                new BigDecimal("50000.00"), PaymentMethod.CARD
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("FromStop 99 not found");

        verify(stopRepository).findById(99L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenToStopNotExists() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);

        var request = new TicketCreateRequest(
                1L, 1L, "A1", 1L, 99L,
                new BigDecimal("50000.00"), PaymentMethod.CARD
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ToStop 99 not found");

        verify(stopRepository).findById(99L);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenFromStopNotBelongToRoute() {
        // Given
        var route = createTestRoute();
        var otherRoute = Route.builder().id(2L).code("RUT-002").build();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(otherRoute, "Another Terminal", 1); // Different route!
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var seat = Seat.builder()
                .id(1L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var request = new TicketCreateRequest(
                1L, 1L, "A1", 1L, 5L,
                new BigDecimal("50000.00"), PaymentMethod.CARD
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(bus.getId(), "A1")).thenReturn(Optional.of(seat));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FromStop doesn't belong to trip's route");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenToStopNotBelongToRoute() {
        // Given
        var route = createTestRoute();
        var otherRoute = Route.builder().id(2L).code("RUT-002").build();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(otherRoute, "Another Terminal", 5); // Different route!

        var seat = Seat.builder()
                .id(1L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var request = new TicketCreateRequest(
                1L, 1L, "A1", 1L, 5L,
                new BigDecimal("50000.00"), PaymentMethod.CARD
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(bus.getId(), "A1")).thenReturn(Optional.of(seat));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ToStop does not belong to trip's route");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenFromStopOrderGreaterOrEqualToStopOrder() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Medellin", 5);
        var toStop = createTestStop(route, "Terminal Bogota", 1);

        var seat = Seat.builder()
                .id(1L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var request = new TicketCreateRequest(
                1L, 1L, "A1", 5L, 1L, // fromStop order > toStop order
                new BigDecimal("50000.00"), PaymentMethod.CARD
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(bus.getId(), "A1")).thenReturn(Optional.of(seat));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FromStop order must be less than ToStop order");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSeatAlreadySoldForTrip() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var seat = Seat.builder()
                .id(1L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var request = new TicketCreateRequest(
                1L, 1L, "A1", 1L, 5L,
                new BigDecimal("50000.00"), PaymentMethod.CARD
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(bus.getId(), "A1")).thenReturn(Optional.of(seat));
        when(ticketRepository.existsOverlappingTicket(1L, "A1", 1, 5)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat A1 is already occupied in overlapping segment");

        verify(ticketRepository, never()).save(any());
    }

    // ============= UPDATE TESTS =============

    @Test
    void shouldUpdateTicketSuccessfully() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var existingTicket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        var updateRequest = new TicketUpdateRequest(
                "A2",
                new BigDecimal("55000.00"),
                PaymentMethod.CASH,
                TicketStatus.SOLD
        );

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A2")).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A2");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("55000.00"));
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CASH);

        verify(ticketRepository).findById(10L);
        verify(ticketRepository).findByTripAndSeatNumber(trip, "A2");
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldUpdateTicketWithSameSeatNumber() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var existingTicket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        var updateRequest = new TicketUpdateRequest(
                "A1", // Same seat
                new BigDecimal("55000.00"),
                PaymentMethod.CASH,
                TicketStatus.SOLD
        );

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.seatNumber()).isEqualTo("A1");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("55000.00"));

        verify(ticketRepository).findById(10L);
        verify(ticketRepository, never()).findByTripAndSeatNumber(any(), any());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUpdatingSeatToOccupiedOne() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var existingTicket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        var otherTicket = Ticket.builder()
                .id(11L)
                .trip(trip)
                .seatNumber("A2")
                .build();

        var updateRequest = new TicketUpdateRequest(
                "A2", // Already occupied
                new BigDecimal("55000.00"),
                PaymentMethod.CASH,
                TicketStatus.SOLD
        );

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A2"))
                .thenReturn(Optional.of(otherTicket));

        // When / Then
        assertThatThrownBy(() -> service.update(10L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat A2 already occupied");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentTicket() {
        // Given
        var updateRequest = new TicketUpdateRequest(
                "A2", new BigDecimal("55000.00"), PaymentMethod.CASH, TicketStatus.SOLD
        );
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 99 not found");

        verify(ticketRepository).findById(99L);
        verify(ticketRepository, never()).save(any());
    }

    // ============= GET BY ID TESTS =============

    @Test
    void shouldGetTicketById() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A1");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("50000.00"));

        verify(ticketRepository).findById(10L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentTicket() {
        // Given
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 99 not found");

        verify(ticketRepository).findById(99L);
    }

    // ============= DELETE TESTS =============

    @Test
    void shouldDeleteTicketSuccessfully() {
        // Given
        var ticket = Ticket.builder()
                .id(10L)
                .seatNumber("A1")
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        doNothing().when(ticketRepository).delete(ticket);

        // When
        service.delete(10L);

        // Then
        verify(ticketRepository).findById(10L);
        verify(ticketRepository).delete(ticket);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentTicket() {
        // Given
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 99 not found");

        verify(ticketRepository).findById(99L);
        verify(ticketRepository, never()).delete(any());
    }

    // ============= GET BY QR CODE TESTS =============

    @Test
    void shouldGetTicketByQrCode() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.QR)
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        when(ticketRepository.findByQrCode("QR-ABC123")).thenReturn(Optional.of(ticket));

        // When
        var response = service.getByQrCode("QR-ABC123");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.qrCode()).isEqualTo("QR-ABC123");

        verify(ticketRepository).findByQrCode("QR-ABC123");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenQrCodeNotExists() {
        // Given
        when(ticketRepository.findByQrCode("QR-INVALID")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByQrCode("QR-INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket with QR QR-INVALID not found");

        verify(ticketRepository).findByQrCode("QR-INVALID");
    }

    // ============= GET BY PASSENGER ID TESTS =============

    @Test
    void shouldGetTicketsByPassengerId() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket1 = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        var ticket2 = Ticket.builder()
                .id(11L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A2")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR-DEF456")
                .build();

        when(ticketRepository.findByPassengerId(1L)).thenReturn(List.of(ticket1, ticket2));

        // When
        var result = service.listByPassengerId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).passengerId()).isEqualTo(1L);
        assertThat(result.get(1).passengerId()).isEqualTo(1L);

        verify(ticketRepository).findByPassengerId(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPassengerHasNoTickets() {
        // Given
        when(ticketRepository.findByPassengerId(99L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByPassengerId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No tickets found for passenger 99");

        verify(ticketRepository).findByPassengerId(99L);
    }

// ============= GET BY TRIP ID TESTS =============

    @Test
    void shouldGetTicketsByTripId() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket1 = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        var ticket2 = Ticket.builder()
                .id(11L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A2")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR-DEF456")
                .build();

        when(ticketRepository.findByTripId(1L)).thenReturn(List.of(ticket1, ticket2));

        // When
        var result = service.listByTripId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).tripId()).isEqualTo(1L);
        assertThat(result.get(1).tripId()).isEqualTo(1L);

        verify(ticketRepository).findByTripId(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripHasNoTickets() {
        // Given
        when(ticketRepository.findByTripId(99L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByTripId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No tickets found for trip 99");

        verify(ticketRepository).findByTripId(99L);
    }

    // ============= GET BY PAYMENT METHOD TESTS =============

    @Test
    void shouldGetTicketsByPaymentMethod() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        var pageable = PageRequest.of(0, 10);
        when(ticketRepository.findByPaymentMethod(PaymentMethod.CARD, pageable))
                .thenReturn(new PageImpl<>(List.of(ticket)));

        // When
        var result = service.listByPaymentMethod(PaymentMethod.CARD, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().paymentMethod()).isEqualTo(PaymentMethod.CARD);

        verify(ticketRepository).findByPaymentMethod(PaymentMethod.CARD, pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoTicketsWithPaymentMethod() {
        // Given
        var pageable = PageRequest.of(0, 10);
        when(ticketRepository.findByPaymentMethod(PaymentMethod.QR, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        // When / Then
        assertThatThrownBy(() -> service.listByPaymentMethod(PaymentMethod.QR, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No tickets found with payment method QR");

        verify(ticketRepository).findByPaymentMethod(PaymentMethod.QR, pageable);
    }

    // ============= GET BY STATUS TESTS =============

    @Test
    void shouldGetTicketsByStatus() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L).trip(trip).passenger(passenger).seatNumber("A1").fromStop(fromStop).toStop(toStop).price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD).status(TicketStatus.SOLD).qrCode("QR-ABC123").build();

        var pageable = PageRequest.of(0, 10);
        when(ticketRepository.findByStatus(TicketStatus.SOLD, pageable))
                .thenReturn(new PageImpl<>(List.of(ticket)));

        // When
        var result = service.listByStatus(TicketStatus.SOLD, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo(TicketStatus.SOLD);

        verify(ticketRepository).findByStatus(TicketStatus.SOLD, pageable);
    }

    // ============= GET BETWEEN STOPS TESTS =============

    @Test
    void shouldGetTicketsBetweenStops() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L).trip(trip).passenger(passenger).seatNumber("A1").fromStop(fromStop).toStop(toStop).price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD).status(TicketStatus.SOLD).qrCode("QR-ABC123").build();

        var pageable = PageRequest.of(0, 10);
        when(ticketRepository.findAllBetweenOptionalStops(1L, 5L, pageable))
                .thenReturn(new PageImpl<>(List.of(ticket)));

        // When
        var result = service.listBetweenStops(1L, 5L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().fromStopId()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().toStopId()).isEqualTo(5L);

        verify(ticketRepository).findAllBetweenOptionalStops(1L, 5L, pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoTicketsBetweenStops() {
        // Given
        var pageable = PageRequest.of(0, 10);
        when(ticketRepository.findAllBetweenOptionalStops(1L, 5L, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        // When / Then
        assertThatThrownBy(() -> service.listBetweenStops(1L, 5L, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No tickets found between stops");

        verify(ticketRepository).findAllBetweenOptionalStops(1L, 5L, pageable);
    }

    // ============= GET TOTAL BY PASSENGER ID TESTS =============

    @Test
    void shouldGetTotalByPassengerId() {
        // Given
        var expectedTotal = new BigDecimal("150000.00");
        when(ticketRepository.totalPriceByPassengerId(1L)).thenReturn(expectedTotal);

        // When
        var result = service.getTotalByPassengerId(1L);

        // Then
        assertThat(result).isEqualByComparingTo(expectedTotal);

        verify(ticketRepository).totalPriceByPassengerId(1L);
    }

    @Test
    void shouldReturnZeroWhenPassengerHasNoSoldTickets() {
        // Given
        when(ticketRepository.totalPriceByPassengerId(99L)).thenReturn(null);

        // When
        var result = service.getTotalByPassengerId(99L);

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        verify(ticketRepository).totalPriceByPassengerId(99L);
    }

    // ============= CANCEL TESTS =============

    @Test
    void shouldCancelTicketSuccessfully() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(25)) // Más de 24 horas
                .arrivalTime(OffsetDateTime.now().plusHours(31))
                .status(TripStatus.SCHEDULED)
                .build();
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L).trip(trip).passenger(passenger).seatNumber("A1").fromStop(fromStop).toStop(toStop).price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD).status(TicketStatus.SOLD).qrCode("QR-ABC123").build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(configService.getValue("REFUND_24H_PERCENT")).thenReturn(new BigDecimal("100"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.cancel(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(response.refundAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));

        verify(ticketRepository).findById(10L);
        verify(configService).getValue("REFUND_24H_PERCENT");
        verify(ticketRepository).save(any(Ticket.class));
        verify(notificationService).sendTicketCancellation(
                eq("3001234567"),
                eq("Juan Perez"),
                eq(10L),
                any(BigDecimal.class),
                eq(PaymentMethod.CARD)
        );
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCancelNonExistentTicket() {
        // Given
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.cancel(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 99 not found");

        verify(ticketRepository).findById(99L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCancelNonSoldTicket() {
        // Given
        var ticket = Ticket.builder()
                .id(10L)
                .status(TicketStatus.CANCELLED)
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        // When / Then
        assertThatThrownBy(() -> service.cancel(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only SOLD tickets can be cancelled");

        verify(ticketRepository).findById(10L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCancelNoShowTicket() {
        // Given
        var ticket = Ticket.builder()
                .id(10L)
                .status(TicketStatus.NO_SHOW)
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        // When / Then
        assertThatThrownBy(() -> service.cancel(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only SOLD tickets can be cancelled");

        verify(ticketRepository).findById(10L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldRefundFullAmountWhenCancel24HoursInAdvance() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(25)) // 25 horas en el futuro
                .arrivalTime(OffsetDateTime.now().plusHours(31))
                .status(TripStatus.SCHEDULED)
                .build();
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L).trip(trip).passenger(passenger).seatNumber("A1").fromStop(fromStop).toStop(toStop).price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD).status(TicketStatus.SOLD).qrCode("QR-ABC123")
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(configService.getValue("REFUND_24H_PERCENT")).thenReturn(new BigDecimal("100")); // 100%
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.cancel(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(response.refundAmount()).isEqualByComparingTo(new BigDecimal("50000.00")); // 100% de 50000

        verify(ticketRepository).findById(10L);
        verify(configService).getValue("REFUND_24H_PERCENT");
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldRefund50PercentWhenCancel12HoursInAdvance() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(13)) // 13 horas en el futuro
                .arrivalTime(OffsetDateTime.now().plusHours(19))
                .status(TripStatus.SCHEDULED)
                .build();
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L).trip(trip).passenger(passenger).seatNumber("A1").fromStop(fromStop).toStop(toStop).price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD).status(TicketStatus.SOLD).qrCode("QR-ABC123")
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(configService.getValue("REFUND_12H_PERCENT")).thenReturn(new BigDecimal("50")); // 50%
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.cancel(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(response.refundAmount()).isEqualByComparingTo(new BigDecimal("25000.00")); // 50% de 50000

        verify(ticketRepository).findById(10L);
        verify(configService).getValue("REFUND_12H_PERCENT");
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldRefund0PercentWhenCancel2HoursInAdvance() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(3)) // 3 horas en el futuro
                .arrivalTime(OffsetDateTime.now().plusHours(9))
                .status(TripStatus.SCHEDULED)
                .build();
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L).trip(trip).passenger(passenger).seatNumber("A1").fromStop(fromStop).toStop(toStop).price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD).status(TicketStatus.SOLD).qrCode("QR-ABC123")
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(configService.getValue("REFUND_2H_PERCENT")).thenReturn(new BigDecimal("0")); // 0%
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.cancel(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(response.refundAmount()).isEqualByComparingTo(new BigDecimal("0.00")); // 0% de 50000

        verify(ticketRepository).findById(10L);
        verify(configService).getValue("REFUND_2H_PERCENT");
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCancelLessThan2HoursInAdvance() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(1)) // Solo 1 hora
                .arrivalTime(OffsetDateTime.now().plusHours(7))
                .status(TripStatus.SCHEDULED)
                .build();
        var passenger = createTestPassenger();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var ticket = Ticket.builder()
                .id(10L).trip(trip).passenger(passenger).seatNumber("A1").fromStop(fromStop).toStop(toStop).price(new BigDecimal("50000.00"))
                .paymentMethod(PaymentMethod.CARD).status(TicketStatus.SOLD).qrCode("QR-ABC123")
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        // When / Then
        assertThatThrownBy(() -> service.cancel(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cancellations must be made at least 2 hours before departure");

        verify(ticketRepository).findById(10L);
        verify(ticketRepository, never()).save(any());
    }
}


