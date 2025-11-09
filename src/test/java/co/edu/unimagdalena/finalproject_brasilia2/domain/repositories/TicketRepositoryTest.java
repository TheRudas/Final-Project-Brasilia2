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
    private Trip trip1;
    private Trip trip2;
    private Stop stop1;
    private Stop stop2;
    private Stop stop3;
    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;

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
                .name("Maria La Del Barrio")
                .email("marialadelbarriosoywuju@rcnmail.com")
                .phone("3001111111")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("")
                .createdAt(OffsetDateTime.now())
                .build();
        passenger1 = userRepository.save(passenger1);

        passenger2 = User.builder()
                .name("Pedro Perez")
                .email("pedro@mail.com")
                .phone("3002222222")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("")
                .createdAt(OffsetDateTime.now())
                .build();
        passenger2 = userRepository.save(passenger2);

        // Create route
        Route route = Route.builder()
                .code("R001")
                .name("Bogota-Cartagena")
                .origin("Bogota")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("1000"))
                .durationMin(720)
                .build();
        route = routeRepository.save(route);

        // Create stops
        stop1 = Stop.builder()
                .route(route)
                .name("Terminal Bogota")
                .order(1)
                .build();
        stop1 = stopRepository.save(stop1);

        stop2 = Stop.builder()
                .route(route)
                .name("Terminal Cartagena")
                .order(2)
                .build();
        stop2 = stopRepository.save(stop2);

        stop3 = Stop.builder()
                .route(route)
                .name("Terminal Barranquilla")
                .order(3)
                .build();
        stop3 = stopRepository.save(stop3);

        // Create bus
        Bus bus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();
        bus = busRepository.save(bus);

        // Create trips
        trip1 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusHours(14))
                .status(TripStatus.SCHEDULED)
                .build();
        trip1 = tripRepository.save(trip1);

        trip2 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(OffsetDateTime.now().plusHours(26))
                .arrivalTime(OffsetDateTime.now().plusHours(38))
                .status(TripStatus.SCHEDULED)
                .build();
        trip2 = tripRepository.save(trip2);

        // Create tickets
        ticket1 = Ticket.builder()
                .trip(trip1)
                .passenger(passenger1)
                .seatNumber("A1")
                .fromStop(stop1)
                .toStop(stop2)
                .price(new BigDecimal("150000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR001")
                .build();

        ticket2 = Ticket.builder()
                .trip(trip1)
                .passenger(passenger1)
                .seatNumber("A2")
                .fromStop(stop1)
                .toStop(stop3)
                .price(new BigDecimal("200000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR002")
                .build();

        ticket3 = Ticket.builder()
                .trip(trip2)
                .passenger(passenger2)
                .seatNumber("B1")
                .fromStop(stop1)
                .toStop(stop2)
                .price(new BigDecimal("180000"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(TicketStatus.CANCELLED)
                .qrCode("QR003")
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
                .containsExactlyInAnyOrder("QR001", "QR002"); //Only this attribute is unique. For cleanest assertions
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
        assertThat(result.get().getQrCode()).isEqualTo("QR001");
        assertThat(result.get().getPassenger().getName()).isEqualTo("Maria La Del Barrio");
    }

    @Test
    @DisplayName("Ticket: find by trip id")
    void shouldFindByTripId() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        // When
        var result = ticketRepository.findByTripId(trip1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Ticket::getSeatNumber).containsExactlyInAnyOrder("A1", "A2");
    }

    @Test
    @DisplayName("Ticket: find by QR code")
    void shouldFindByQrCode() {
        // Given
        ticketRepository.save(ticket1);

        // When
        var result = ticketRepository.findByQrCode("QR001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSeatNumber()).isEqualTo("A1");
        assertThat(result.get().getPrice()).isEqualByComparingTo(new BigDecimal("150000"));
    }

    @Test
    @DisplayName("Ticket: find by payment method")
    void shouldFindByPaymentMethod() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        // When
        var cardTickets = ticketRepository.findByPaymentMethod(
                PaymentMethod.CARD,
                PageRequest.of(0, 10)
        );
        var cashTickets = ticketRepository.findByPaymentMethod(
                PaymentMethod.CASH,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(cardTickets.getContent()).hasSize(1);
        assertThat(cardTickets.getContent().get(0).getQrCode()).isEqualTo("QR001");

        assertThat(cashTickets.getContent()).hasSize(1);
        assertThat(cashTickets.getContent().get(0).getQrCode()).isEqualTo("QR002");
    }

    @Test
    @DisplayName("Ticket: find by status")
    void shouldFindByStatus() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        // When
        var soldTickets = ticketRepository.findByStatus(TicketStatus.SOLD, PageRequest.of(0, 10));

        var cancelledTickets = ticketRepository.findByStatus(TicketStatus.CANCELLED, PageRequest.of(0, 10));

        // Then
        assertThat(soldTickets.getContent()).hasSize(2);
        assertThat(soldTickets.getContent())
                .extracting(Ticket::getQrCode)
                .containsExactlyInAnyOrder("QR001", "QR002");

        assertThat(cancelledTickets.getContent()).hasSize(1);
        assertThat(cancelledTickets.getContent().get(0).getQrCode()).isEqualTo("QR003");
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
        assertThat(total).isEqualByComparingTo(new BigDecimal("350000")); //150k + 200k
    }

    @Test
    @DisplayName("Ticket: find all between optional stops")
    void shouldFindAllBetweenOptionalStops() {
        // Given
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        // When
        var fromStop1 = ticketRepository.findAllBetweenOptionalStops(stop1.getId(), null, PageRequest.of(0, 10));

        var toStop2 = ticketRepository.findAllBetweenOptionalStops(null, stop2.getId(), PageRequest.of(0, 10));

        var bothStops = ticketRepository.findAllBetweenOptionalStops(stop1.getId(), stop2.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(fromStop1.getContent()).hasSize(3);

        assertThat(toStop2.getContent()).hasSize(2);
        assertThat(toStop2.getContent())
                .extracting(Ticket::getQrCode).containsExactlyInAnyOrder("QR001", "QR003");

        assertThat(bothStops.getContent()).hasSize(2);
        assertThat(bothStops.getContent())
                .extracting(Ticket::getQrCode).containsExactlyInAnyOrder("QR001", "QR003");
    }

    @Test
    @DisplayName("Ticket: return empty when QR code not found")
    void shouldReturnEmptyWhenQrCodeNotFound() {
        // Given
        ticketRepository.save(ticket1);

        // When
        var result = ticketRepository.findByQrCode("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Ticket: return null when passenger has no sold tickets")
    void shouldReturnNullWhenNoSoldTickets() {
        // Given
        ticketRepository.save(ticket3); // CANCELLED

        // When
        var total = ticketRepository.totalPriceByPassengerId(passenger2.getId());

        // Then
        assertThat(total).isNull();
    }

    @Test
    @DisplayName("Ticket: return empty list when passenger has no tickets")
    void shouldReturnEmptyWhenPassengerHasNoTickets() {
        // Given - no tickets saved, where flowers come from if there's no garden?

        // When
        var result = ticketRepository.findByPassengerId(999L);

        // Then
        assertThat(result).isEmpty();
    }
}