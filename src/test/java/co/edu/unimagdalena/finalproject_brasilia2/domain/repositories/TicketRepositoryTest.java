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
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private StopRepository stopRepository;

    private User passenger1;
    private User passenger2;
    private Trip trip;
    private Stop fromStop;
    private Stop toStop;
    private Ticket ticketSold;
    private Ticket ticketCancelled;
    private Ticket ticketNoShow;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();
        stopRepository.deleteAll();

        // Create passengers
        passenger1 = User.builder()
                .name("Juan Perez")
                .email("juan@mail.com")
                .phone("3001111111")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hash123")
                .createdAt(OffsetDateTime.now())
                .build();
        userRepository.save(passenger1);

        passenger2 = User.builder()
                .name("Maria Garcia")
                .email("maria@mail.com")
                .phone("3002222222")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hash456")
                .createdAt(OffsetDateTime.now())
                .build();
        userRepository.save(passenger2);

        // Create route
        Route route = Route.builder()
                .code("R001")
                .name("Bogota-Cartagena")
                .origin("Bogota")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("1000"))
                .durationMin(720)
                .build();
        routeRepository.save(route);

        // Create stops
        fromStop = Stop.builder()
                .route(route)
                .name("Terminal Bogota")
                .order(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();
        stopRepository.save(fromStop);

        toStop = Stop.builder()
                .route(route)
                .name("Terminal Cartagena")
                .order(2)
                .lat(10.3910)
                .lng(-75.4794)
                .build();
        stopRepository.save(toStop);

        // Create bus
        Bus bus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();
        busRepository.save(bus);

        // Create trip
        trip = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().minusHours(3)) // Departed 3 hours ago
                .arrivalTime(OffsetDateTime.now().plusHours(9))
                .status(TripStatus.DEPARTED)
                .build();
        tripRepository.save(trip);

        // Create tickets with different statuses
        ticketSold = Ticket.builder()
                .trip(trip)
                .passenger(passenger1)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("150000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-001")
                .build();

        ticketCancelled = Ticket.builder()
                .trip(trip)
                .passenger(passenger1)
                .seatNumber("A2")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("150000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.CANCELLED)
                .qrCode("QR-003")
                .build();

        ticketNoShow = Ticket.builder()
                .trip(trip)
                .passenger(passenger2)
                .seatNumber("B1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("180000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-002")
                .build();
    }

    @Test
    @DisplayName("Ticket: find by passenger id")
    void shouldFindByPassengerId() {
        // Given
        ticketRepository.save(ticketSold);
        ticketRepository.save(ticketCancelled);
        ticketRepository.save(ticketNoShow);

        // When
        var result = ticketRepository.findByPassengerId(passenger1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Ticket::getSeatNumber)
                .containsExactlyInAnyOrder("A1", "A2");
    }

    @Test
    @DisplayName("Ticket: find by trip and seat number")
    void shouldFindByTripAndSeatNumber() {
        // Given
        ticketRepository.save(ticketSold);

        // When
        var result = ticketRepository.findByTripAndSeatNumber(trip, "A1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getQrCode()).isEqualTo("QR-001");
    }

    @Test
    @DisplayName("Ticket: find by trip id")
    void shouldFindByTripId() {
        // Given
        ticketRepository.save(ticketSold);
        ticketRepository.save(ticketCancelled);
        ticketRepository.save(ticketNoShow);

        // When
        var result = ticketRepository.findByTripId(trip.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Ticket::getSeatNumber)
                .containsExactlyInAnyOrder("A1", "A2", "B1");
    }

    @Test
    @DisplayName("Ticket: find by QR code")
    void shouldFindByQrCode() {
        // Given
        ticketRepository.save(ticketSold);

        // When
        var result = ticketRepository.findByQrCode("QR-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSeatNumber()).isEqualTo("A1");
    }

    @Test
    @DisplayName("Ticket: find by payment method")
    void shouldFindByPaymentMethod() {
        // Given
        ticketRepository.save(ticketSold);
        ticketRepository.save(ticketCancelled);
        ticketRepository.save(ticketNoShow);

        // When
        var result = ticketRepository.findByPaymentMethod(
                PaymentMethod.CARD,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Ticket::getQrCode)
                .containsExactlyInAnyOrder("QR-001", "QR-002");
    }

    @Test
    @DisplayName("Ticket: find by status")
    void shouldFindByStatus() {
        // Given
        ticketRepository.save(ticketSold);
        ticketRepository.save(ticketCancelled);
        ticketRepository.save(ticketNoShow);

        // When
        var result = ticketRepository.findByStatus(
                TicketStatus.SOLD,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Ticket::getSeatNumber)
                .containsExactlyInAnyOrder("A1", "B1");
    }

    @Test
    @DisplayName("Ticket: calculate total price by passenger id")
    void shouldCalculateTotalPriceByPassengerId() {
        // Given
        ticketRepository.save(ticketSold);
        ticketRepository.save(ticketCancelled); // CANCELLED shouldn't count

        Ticket anotherSoldTicket = Ticket.builder()
                .trip(trip)
                .passenger(passenger1)
                .seatNumber("A3")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("200000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-SOLD-002")
                .build();
        ticketRepository.save(anotherSoldTicket);

        // When
        BigDecimal total = ticketRepository.totalPriceByPassengerId(passenger1.getId());

        // Then
        assertThat(total).isEqualByComparingTo(new BigDecimal("350000")); // 150000 + 200000
    }

    @Test
    @DisplayName("Ticket: find all between optional stops")
    void shouldFindAllBetweenOptionalStops() {
        // Given
        ticketRepository.save(ticketSold);
        ticketRepository.save(ticketNoShow);

        // When - both stops specified
        var resultBoth = ticketRepository.findAllBetweenOptionalStops(
                fromStop.getId(),
                toStop.getId(),
                PageRequest.of(0, 10)
        );

        // When - only fromStop
        var resultFrom = ticketRepository.findAllBetweenOptionalStops(
                fromStop.getId(),
                null,
                PageRequest.of(0, 10)
        );

        // When - only toStop
        var resultTo = ticketRepository.findAllBetweenOptionalStops(
                null,
                toStop.getId(),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(resultBoth.getContent()).hasSize(2);
        assertThat(resultFrom.getContent()).hasSize(2);
        assertThat(resultTo.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Ticket: find no-shows")
    void shouldFindNoShows() {
        // Given
        ticketRepository.save(ticketSold);
        ticketRepository.save(ticketNoShow);

        // When - threshold is now (3 hours after departure)
        OffsetDateTime threshold = OffsetDateTime.now();
        var result = ticketRepository.findNoShows(threshold);

        // Then - both SOLD tickets with past departure should be found
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Ticket::getStatus)
                .containsOnly(TicketStatus.SOLD);
    }

    @Test
    @DisplayName("Ticket: return empty when QR code not found")
    void shouldReturnEmptyWhenQrCodeNotFound() {
        // Given
        ticketRepository.save(ticketSold);

        // When
        var result = ticketRepository.findByQrCode("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Ticket: return empty when trip and seat not found")
    void shouldReturnEmptyWhenTripAndSeatNotFound() {
        // Given
        ticketRepository.save(ticketSold);

        // When
        var result = ticketRepository.findByTripAndSeatNumber(trip, "Z99");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Ticket: return zero when passenger has no sold tickets")
    void shouldReturnZeroWhenNoSoldTickets() {
        // Given
        ticketRepository.save(ticketCancelled); // Only cancelled ticket

        // When
        BigDecimal total = ticketRepository.totalPriceByPassengerId(passenger1.getId());

        // Then
        assertThat(total).isNull(); // SUM returns null when no rows match
    }
}