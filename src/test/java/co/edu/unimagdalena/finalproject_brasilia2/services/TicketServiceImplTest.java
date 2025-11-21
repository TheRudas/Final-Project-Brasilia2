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
import java.time.ZoneOffset;
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
    private SeatRepository seatRepository;

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private ConfigService configService;

    @Mock
    private FareRuleService fareRuleService;

    @Spy
    private TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @InjectMocks
    private TicketServiceImpl service;

    @Test
    void shouldCreateTicketSuccessfully() {
        // Given
        var passenger = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hash")
                .createdAt(OffsetDateTime.now())
                .build();

        var route = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Bogotá-Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKm(new BigDecimal("400.00"))
                .durationMin(360)
                .build();

        var bus = Bus.builder()
                .id(2L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var trip = Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .date(LocalDate.of(2025, 12, 20))
                .departureTime(OffsetDateTime.of(2025, 12, 20, 8, 0, 0, 0, ZoneOffset.UTC))
                .arrivalTime(OffsetDateTime.of(2025, 12, 20, 16, 0, 0, 0, ZoneOffset.UTC))
                .status(TripStatus.SCHEDULED)
                .build();

        var fromStop = Stop.builder()
                .id(1L)
                .route(route)
                .name("Terminal Bogotá")
                .order(1)
                .lat(4.7110)
                .lng(-74.0721)
                .build();

        var toStop = Stop.builder()
                .id(5L)
                .route(route)
                .name("Terminal Medellín")
                .order(5)
                .lat(6.2442)
                .lng(-75.5812)
                .build();

        var seat = Seat.builder()
                .id(1L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var request = new TicketCreateRequest(
                1L,                              // tripId
                1L,                              // passengerId
                "A1",                            // seatNumber
                1L,                              // fromStopId
                5L,                              // toStopId
                new BigDecimal("50000.00"),     // price (será recalculado por FareRuleService)
                PaymentMethod.CARD,
                PassengerType.ADULT             // passengerType
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(bus.getId(), "A1")).thenReturn(Optional.of(seat));
        when(ticketRepository.existsOverlappingTicket(1L, "A1", 1, 5)).thenReturn(false);
        when(seatHoldRepository.findByTripIdAndSeatNumberAndStatus(1L, "A1", SeatHoldStatus.HOLD))
                .thenReturn(Optional.empty());
        when(fareRuleService.calculateTicketPrice(eq(1L), eq(1L), eq(5L), any()))
                .thenReturn(new BigDecimal("50000.00"));
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
        assertThat(response.status()).isEqualTo(TicketStatus.SOLD);
        assertThat(response.passengerType()).isEqualTo(PassengerType.ADULT);
        assertThat(response.qrCode()).startsWith("QR-"); // Auto-generado
        assertThat(response.departureAt()).isEqualTo(OffsetDateTime.of(2025, 12, 20, 8, 0, 0, 0, ZoneOffset.UTC));

        verify(tripRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(stopRepository).findById(1L);
        verify(stopRepository).findById(5L);
        verify(seatRepository).findByBusIdAndNumber(bus.getId(), "A1");
        verify(ticketRepository).existsOverlappingTicket(1L, "A1", 1, 5);
        verify(fareRuleService).calculateTicketPrice(eq(1L), eq(1L), eq(5L), eq(PassengerType.ADULT));
        verify(seatHoldRepository).findByTripIdAndSeatNumberAndStatus(1L, "A1", SeatHoldStatus.HOLD);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExists() {
        // Given
        var request = new TicketCreateRequest(
                99L,
                5L,
                "A1",
                10L,
                11L,
                new BigDecimal("50000"),
                PaymentMethod.CASH,
                PassengerType.ADULT
        );

        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(tripRepository).findById(99L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPassengerNotExists() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var request = new TicketCreateRequest(
                100L,
                99L,
                "A1",
                10L,
                11L,
                new BigDecimal("50000"),
                PaymentMethod.CASH,
                PassengerType.ADULT
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Passenger 99 not found");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenSeatNotExistsInBus() {
        // Given
        var bus = Bus.builder().id(10L).plate("ABC123").build();
        var route = Route.builder().id(1L).build();
        var trip = Trip.builder().id(100L).bus(bus).route(route).build();
        var passenger = User.builder().id(5L).build();
        var fromStop = Stop.builder().id(10L).route(route).order(1).build();
        var toStop = Stop.builder().id(11L).route(route).order(5).build();

        var request = new TicketCreateRequest(
                100L,
                5L,
                "Z99",
                10L,
                11L,
                new BigDecimal("50000"),
                PaymentMethod.CASH,
                PassengerType.ADULT
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(5L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(10L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(11L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(10L, "Z99")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Seat Z99 does not exist in bus ABC123");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenStopsOrderInvalid() {
        // Given
        var bus = Bus.builder().id(10L).build();
        var route = Route.builder().id(1L).build();
        var trip = Trip.builder().id(100L).bus(bus).route(route).build();
        var passenger = User.builder().id(5L).build();
        var fromStop = Stop.builder().id(10L).route(route).order(5).build();
        var toStop = Stop.builder().id(11L).route(route).order(1).build();
        var seat = Seat.builder().id(1L).bus(bus).number("A1").build();

        var request = new TicketCreateRequest(
                100L,
                5L,
                "A1",
                10L,
                11L,
                new BigDecimal("50000"),
                PaymentMethod.CASH,
                PassengerType.ADULT
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(5L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(10L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(11L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(10L, "A1")).thenReturn(Optional.of(seat));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FromStop order must be less than ToStop order");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSeatOverlaps() {
        // Given
        var bus = Bus.builder().id(10L).build();
        var route = Route.builder().id(1L).build();
        var trip = Trip.builder().id(100L).bus(bus).route(route).build();
        var passenger = User.builder().id(5L).build();
        var fromStop = Stop.builder().id(10L).route(route).order(1).build();
        var toStop = Stop.builder().id(11L).route(route).order(5).build();
        var seat = Seat.builder().id(1L).bus(bus).number("A1").build();

        var request = new TicketCreateRequest(
                100L,
                5L,
                "A1",
                10L,
                11L,
                new BigDecimal("50000"),
                PaymentMethod.CASH,
                PassengerType.ADULT
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(5L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(10L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(11L)).thenReturn(Optional.of(toStop));
        when(seatRepository.findByBusIdAndNumber(10L, "A1")).thenReturn(Optional.of(seat));
        when(ticketRepository.existsOverlappingTicket(100L, "A1", 1, 5)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat A1 is already occupied in overlapping segment");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTicketSuccessfully() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var passenger = User.builder().id(5L).build();

        var existingTicket = Ticket.builder()
                .id(1000L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .price(new BigDecimal("50000"))
                .status(TicketStatus.SOLD)
                .build();

        var updateRequest = new TicketUpdateRequest(
                "A2",
                new BigDecimal("55000"),
                PaymentMethod.CARD,
                TicketStatus.SOLD
        );

        when(ticketRepository.findById(1000L)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A2")).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1000L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(1000L);
        assertThat(response.seatNumber()).isEqualTo("A2");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("55000"));

        verify(ticketRepository).findById(1000L);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentTicket() {
        // Given
        var updateRequest = new TicketUpdateRequest(
                "A2",
                new BigDecimal("55000"),
                PaymentMethod.CARD,
                TicketStatus.SOLD
        );

        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket 99 not found");

        verify(ticketRepository).findById(99L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUpdateToOccupiedSeat() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var existingTicket = Ticket.builder()
                .id(1000L)
                .trip(trip)
                .seatNumber("A1")
                .build();

        var anotherTicket = Ticket.builder()
                .id(1001L)
                .trip(trip)
                .seatNumber("A2")
                .build();

        var updateRequest = new TicketUpdateRequest(
                "A2",
                new BigDecimal("55000"),
                PaymentMethod.CARD,
                TicketStatus.SOLD
        );

        when(ticketRepository.findById(1000L)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A2")).thenReturn(Optional.of(anotherTicket));

        // When / Then
        assertThatThrownBy(() -> service.update(1000L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat A2 already occupied");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldGetTicketById() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var passenger = User.builder().id(5L).name("Juan Perez").build();

        var ticket = Ticket.builder()
                .id(1000L)
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .price(new BigDecimal("50000"))
                .status(TicketStatus.SOLD)
                .qrCode("QR-ABC123")
                .build();

        when(ticketRepository.findById(1000L)).thenReturn(Optional.of(ticket));

        // When
        var response = service.get(1000L);

        // Then
        assertThat(response.id()).isEqualTo(1000L);
        assertThat(response.seatNumber()).isEqualTo("A1");

        verify(ticketRepository).findById(1000L);
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

    @Test
    void shouldGetTicketByQrCode() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var passenger = User.builder().id(5L).build();

        var ticket = Ticket.builder()
                .id(1000L)
                .trip(trip)
                .passenger(passenger)
                .qrCode("QR-ABC123")
                .build();

        when(ticketRepository.findByQrCode("QR-ABC123")).thenReturn(Optional.of(ticket));

        // When
        var response = service.getByQrCode("QR-ABC123");

        // Then
        assertThat(response.qrCode()).isEqualTo("QR-ABC123");

        verify(ticketRepository).findByQrCode("QR-ABC123");
    }

    @Test
    void shouldListTicketsByPassengerId() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var passenger = User.builder().id(5L).build();

        var ticket1 = Ticket.builder().id(1000L).trip(trip).passenger(passenger).seatNumber("A1").build();
        var ticket2 = Ticket.builder().id(1001L).trip(trip).passenger(passenger).seatNumber("A2").build();

        when(ticketRepository.findByPassengerId(5L))
                .thenReturn(List.of(ticket1, ticket2));

        // When
        var result = service.listByPassengerId(5L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).passengerId()).isEqualTo(5L);
        assertThat(result.get(1).passengerId()).isEqualTo(5L);

        verify(ticketRepository).findByPassengerId(5L);
    }

    @Test
    void shouldListTicketsByTripId() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var passenger = User.builder().id(5L).build();

        var ticket = Ticket.builder().id(1000L).trip(trip).passenger(passenger).build();

        when(ticketRepository.findByTripId(100L))
                .thenReturn(List.of(ticket));

        // When
        var result = service.listByTripId(100L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).tripId()).isEqualTo(100L);

        verify(ticketRepository).findByTripId(100L);
    }

    @Test
    void shouldListTicketsByPaymentMethod() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var passenger = User.builder().id(5L).build();

        var ticket = Ticket.builder()
                .id(1000L)
                .trip(trip)
                .passenger(passenger)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(ticket));

        when(ticketRepository.findByPaymentMethod(PaymentMethod.CASH, pageable))
                .thenReturn(page);

        // When
        var result = service.listByPaymentMethod(PaymentMethod.CASH, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).paymentMethod()).isEqualTo(PaymentMethod.CASH);

        verify(ticketRepository).findByPaymentMethod(PaymentMethod.CASH, pageable);
    }

    @Test
    void shouldListTicketsByStatus() {
        // Given
        var trip = Trip.builder().id(100L).build();
        var passenger = User.builder().id(5L).build();

        var ticket = Ticket.builder()
                .id(1000L)
                .trip(trip)
                .passenger(passenger)
                .status(TicketStatus.SOLD)
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(ticket));

        when(ticketRepository.findByStatus(TicketStatus.SOLD, pageable))
                .thenReturn(page);

        // When
        var result = service.listByStatus(TicketStatus.SOLD, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(TicketStatus.SOLD);

        verify(ticketRepository).findByStatus(TicketStatus.SOLD, pageable);
    }

    @Test
    void shouldCancelTicketWithFullRefund() {
        // Given
        var trip = Trip.builder()
                .id(100L)
                .departureTime(OffsetDateTime.now().plusHours(30))
                .build();

        var ticket = Ticket.builder()
                .id(1000L)
                .trip(trip)
                .price(new BigDecimal("100000"))
                .status(TicketStatus.SOLD)
                .build();

        when(ticketRepository.findById(1000L)).thenReturn(Optional.of(ticket));
        when(configService.getValue("REFUND_24H_PERCENT")).thenReturn(new BigDecimal("100"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.cancel(1000L);

        // Then
        assertThat(response.status()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(response.refundAmount()).isEqualByComparingTo(new BigDecimal("100000.00"));

        verify(ticketRepository).findById(1000L);
        verify(configService).getValue("REFUND_24H_PERCENT");
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCancelNonSoldTicket() {
        // Given
        var ticket = Ticket.builder()
                .id(1000L)
                .status(TicketStatus.CANCELLED)
                .build();

        when(ticketRepository.findById(1000L)).thenReturn(Optional.of(ticket));

        // When / Then
        assertThatThrownBy(() -> service.cancel(1000L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only SOLD tickets can be cancelled");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTicketSuccessfully() {
        // Given
        var ticket = Ticket.builder()
                .id(1000L)
                .seatNumber("A1")
                .build();

        when(ticketRepository.findById(1000L)).thenReturn(Optional.of(ticket));
        doNothing().when(ticketRepository).delete(ticket);

        // When
        service.delete(1000L);

        // Then
        verify(ticketRepository).findById(1000L);
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

    @Test
    void shouldGetTotalByPassengerId() {
        // Given
        when(ticketRepository.totalPriceByPassengerId(5L))
                .thenReturn(new BigDecimal("250000"));

        // When
        var total = service.getTotalByPassengerId(5L);

        // Then
        assertThat(total).isEqualByComparingTo(new BigDecimal("250000"));

        verify(ticketRepository).totalPriceByPassengerId(5L);
    }
}

