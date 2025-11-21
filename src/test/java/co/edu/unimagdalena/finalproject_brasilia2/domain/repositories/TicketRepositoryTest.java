package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TicketRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private StopRepository stopRepository;

    private User passenger1;
    private User passenger2;
    private User clerk1;
    private Trip trip1;
    private Stop stop1;
    private Stop stop2;
    private Stop stop3;
    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;
    private Ticket ticket4;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        stopRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();

        // Create route
        Route route = Route.builder()
                .code("R001")
                .name("Bogota-Medellin")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400"))
                .durationMin(480)
                .build();
        route = routeRepository.save(route);

        // Create stops
        stop1 = Stop.builder()
                .route(route)
                .name("Terminal Bogota")
                .order(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();
        stop1 = stopRepository.save(stop1);

        stop2 = Stop.builder()
                .route(route)
                .name("Terminal Girardot")
                .order(2)
                .lat(4.3122)
                .lng(-74.8030)
                .build();
        stop2 = stopRepository.save(stop2);

        stop3 = Stop.builder()
                .route(route)
                .name("Terminal Medellin")
                .order(3)
                .lat(6.2476)
                .lng(-75.5658)
                .build();
        stop3 = stopRepository.save(stop3);

        // Create bus
        Bus bus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();
        bus = busRepository.save(bus);

        // Create trip
        trip1 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(OffsetDateTime.now().plusDays(1))
                .arrivalTime(OffsetDateTime.now().plusDays(1).plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();
        trip1 = tripRepository.save(trip1);

        // Create users
        passenger1 = User.builder()
                .name("Juan Perez")
                .email("juan@mail.com")
                .phone("3001234567")
                .passwordHash("hash123")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        passenger1 = userRepository.save(passenger1);

        passenger2 = User.builder()
                .name("Maria Garcia")
                .email("maria@mail.com")
                .phone("3107654321")
                .passwordHash("hash456")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        passenger2 = userRepository.save(passenger2);

        clerk1 = User.builder()
                .name("Carlos Clerk")
                .email("carlos@mail.com")
                .phone("3009876543")
                .passwordHash("hash789")
                .role(UserRole.CLERK)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        clerk1 = userRepository.save(clerk1);

        // Create tickets
        ticket1 = Ticket.builder()
                .trip(trip1)
                .passenger(passenger1)
                .fromStop(stop1)
                .toStop(stop3)
                .seatNumber("A1")
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .passengerType(PassengerType.ADULT)
                .qrCode("QR-001")
                .build();

        ticket2 = Ticket.builder()
                .trip(trip1)
                .passenger(passenger1)
                .fromStop(stop1)
                .toStop(stop2)
                .seatNumber("A2")
                .price(new BigDecimal("25000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .passengerType(PassengerType.STUDENT)
                .qrCode("QR-002")
                .build();

        ticket3 = Ticket.builder()
                .trip(trip1)
                .passenger(passenger2)
                .fromStop(stop1)
                .toStop(stop3)
                .seatNumber("B1")
                .price(new BigDecimal("40000"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(TicketStatus.CANCELLED)
                .passengerType(PassengerType.CHILD)
                .qrCode("QR-003")
                .refundAmount(new BigDecimal("35000"))
                .approvedBy(clerk1)
                .build();

        ticket4 = Ticket.builder()
                .trip(trip1)
                .passenger(passenger2)
                .fromStop(stop2)
                .toStop(stop3)
                .seatNumber("B2")
                .price(new BigDecimal("30000"))
                .paymentMethod(PaymentMethod.QR)
                .status(TicketStatus.NO_SHOW)
                .passengerType(PassengerType.ADULT)
                .qrCode("QR-004")
                .noShowFee(new BigDecimal("10000"))
                .build();
    }

    @Test
    @DisplayName("Ticket: find by passenger id")
    void shouldFindByPassengerId() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        // When
        var result = ticketRepository.findByPassengerId(passenger1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Ticket::getQrCode)
                .containsExactlyInAnyOrder("QR-001", "QR-002");
    }

    @Test
    @DisplayName("Ticket: find by trip and seat number")
    void shouldFindByTripAndSeatNumber() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        // When
        var result = ticketRepository.findByTripAndSeatNumber(trip1, "A1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPassenger().getName()).isEqualTo("Juan Perez");
        assertThat(result.get().getPrice()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Ticket: find by trip id")
    void shouldFindByTripId() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        ticketRepository.save(ticket4);

        // When
        var result = ticketRepository.findByTripId(trip1.getId());

        // Then
        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("Ticket: find by QR code")
    void shouldFindByQrCode() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        // When
        var result = ticketRepository.findByQrCode("QR-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSeatNumber()).isEqualTo("A1");
        assertThat(result.get().getPassenger().getName()).isEqualTo("Juan Perez");
    }

    @Test
    @DisplayName("Ticket: find by payment method with pagination")
    void shouldFindByPaymentMethod() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        // When
        var cardTickets = ticketRepository.findByPaymentMethod(PaymentMethod.CARD, PageRequest.of(0, 10));
        var cashTickets = ticketRepository.findByPaymentMethod(PaymentMethod.CASH, PageRequest.of(0, 10));

        // Then
        assertThat(cardTickets.getContent()).hasSize(1);
        assertThat(cardTickets.getContent().get(0).getQrCode()).isEqualTo("QR-001");

        assertThat(cashTickets.getContent()).hasSize(1);
        assertThat(cashTickets.getContent().get(0).getQrCode()).isEqualTo("QR-002");
    }

    @Test
    @DisplayName("Ticket: find by status with pagination")
    void shouldFindByStatus() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        ticketRepository.save(ticket4);

        // When
        var soldTickets = ticketRepository.findByStatus(TicketStatus.SOLD, PageRequest.of(0, 10));
        var cancelledTickets = ticketRepository.findByStatus(TicketStatus.CANCELLED, PageRequest.of(0, 10));

        // Then
        assertThat(soldTickets.getContent()).hasSize(2);
        assertThat(cancelledTickets.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Ticket: calculate total price by passenger id")
    void shouldCalculateTotalPriceByPassengerId() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        // When
        var total = ticketRepository.totalPriceByPassengerId(passenger1.getId());

        // Then
        // 50000 + 25000 = 75000
        assertThat(total).isEqualByComparingTo(new BigDecimal("75000"));
    }

    @Test
    @DisplayName("Ticket: find all between optional stops")
    void shouldFindAllBetweenOptionalStops() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);
        ticketRepository.save(ticket4);

        // When
        var fromStop1 = ticketRepository.findAllBetweenOptionalStops(
                stop1.getId(),
                null,
                PageRequest.of(0, 10)
        );
        var toStop3 = ticketRepository.findAllBetweenOptionalStops(
                null,
                stop3.getId(),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(fromStop1.getContent()).hasSize(3);
        assertThat(toStop3.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("Ticket: find no shows")
    void shouldFindNoShows() {
        // Given
        Trip pastTrip = Trip.builder()
                .route(trip1.getRoute())
                .bus(trip1.getBus())
                .date(LocalDate.now().minusDays(1))
                .departureTime(OffsetDateTime.now().minusDays(1))
                .arrivalTime(OffsetDateTime.now().minusDays(1).plusHours(8))
                .status(TripStatus.DEPARTED)
                .build();
        pastTrip = tripRepository.save(pastTrip);

        Ticket pastTicket = Ticket.builder()
                .trip(pastTrip)
                .passenger(passenger1)
                .fromStop(stop1)
                .toStop(stop3)
                .seatNumber("C1")
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .passengerType(PassengerType.ADULT)
                .qrCode("QR-PAST-001")
                .build();
        ticketRepository.save(pastTicket);

        // When
        var result = ticketRepository.findNoShows(OffsetDateTime.now());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQrCode()).isEqualTo("QR-PAST-001");
    }

    @Test
    @DisplayName("Ticket: check exists overlapping ticket")
    void shouldCheckExistsOverlappingTicket() {
        // Given
        ticketRepository.save(ticket1); // stop1 (order 1) to stop3 (order 3)

        // When
        var overlaps = ticketRepository.existsOverlappingTicket(
                trip1.getId(),
                "A1",
                1,
                2
        );
        var noOverlap = ticketRepository.existsOverlappingTicket(
                trip1.getId(),
                "A1",
                4,
                5
        );

        // Then
        assertThat(overlaps).isTrue();
        assertThat(noOverlap).isFalse();
    }

    @Test
    @DisplayName("Ticket: return empty when QR code not found")
    void shouldReturnEmptyWhenQrCodeNotFound() {
        // Given
        ticketRepository.save(ticket1);

        // When
        var result = ticketRepository.findByQrCode("QR-999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Ticket: return empty when trip and seat not found")
    void shouldReturnEmptyWhenTripAndSeatNotFound() {
        // Given
        ticketRepository.save(ticket1);

        // When
        var result = ticketRepository.findByTripAndSeatNumber(trip1, "Z99");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Ticket: return empty list when passenger has no tickets")
    void shouldReturnEmptyWhenPassengerHasNoTickets() {
        // Given - no tickets for passenger

        // When
        var result = ticketRepository.findByPassengerId(passenger1.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Ticket: return zero when passenger has no sold tickets")
    void shouldReturnZeroWhenPassengerHasNoSoldTickets() {
        // Given
        ticketRepository.save(ticket3); // cancelled ticket

        // When
        var total = ticketRepository.totalPriceByPassengerId(passenger2.getId());

        // Then
        assertThat(total).isNull();
    }

    @Test
    @DisplayName("Ticket: verify refund and approved by fields")
    void shouldVerifyRefundAndApprovedByFields() {
        // Given
        ticketRepository.save(ticket3);

        // When
        var result = ticketRepository.findByQrCode("QR-003");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRefundAmount()).isEqualByComparingTo(new BigDecimal("35000"));
        assertThat(result.get().getApprovedBy()).isNotNull();
        assertThat(result.get().getApprovedBy().getName()).isEqualTo("Carlos Clerk");
    }

    @Test
    @DisplayName("Ticket: verify no show fee field")
    void shouldVerifyNoShowFeeField() {
        // Given
        ticketRepository.save(ticket4);

        // When
        var result = ticketRepository.findByQrCode("QR-004");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNoShowFee()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.get().getStatus()).isEqualTo(TicketStatus.NO_SHOW);
    }
}