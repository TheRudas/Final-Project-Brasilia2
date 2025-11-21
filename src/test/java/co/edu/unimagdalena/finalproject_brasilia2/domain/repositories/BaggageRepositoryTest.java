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

public class BaggageRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private BaggageRepository baggageRepository;

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
    private Stop stop1;
    private Stop stop2;
    private Ticket ticket1;
    private Ticket ticket2;
    private Baggage baggage1;
    private Baggage baggage2;
    private Baggage baggage3;

    @BeforeEach
    void setUp() {
        baggageRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();
        tripRepository.deleteAll();
        stopRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();

        // Create route
        Route route = Route.builder()
                .code("R001")
                .name("Medellin-Cartagena")
                .origin("Medellin")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("650"))
                .durationMin(600)
                .build();
        route = routeRepository.save(route);

        // Create stops
        stop1 = Stop.builder()
                .route(route)
                .name("Terminal Medellin")
                .order(1)
                .lat(6.2476)
                .lng(-75.5658)
                .build();
        stop1 = stopRepository.save(stop1);

        stop2 = Stop.builder()
                .route(route)
                .name("Terminal Cartagena")
                .order(2)
                .lat(10.3910)
                .lng(-75.4794)
                .build();
        stop2 = stopRepository.save(stop2);

        // Create bus
        Bus bus = Bus.builder()
                .plate("XYZ789")
                .capacity(45)
                .status(true)
                .build();
        bus = busRepository.save(bus);

        // Create trip
        trip1 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(OffsetDateTime.now().plusDays(1))
                .arrivalTime(OffsetDateTime.now().plusDays(1).plusHours(10))
                .status(TripStatus.SCHEDULED)
                .build();
        trip1 = tripRepository.save(trip1);

        // Create passengers
        passenger1 = User.builder()
                .name("Pedro Lopez")
                .email("pedro@mail.com")
                .phone("3001234567")
                .passwordHash("hashed_pass_123")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        passenger1 = userRepository.save(passenger1);

        passenger2 = User.builder()
                .name("Ana Martinez")
                .email("ana@mail.com")
                .phone("3107654321")
                .passwordHash("hashed_pass_456")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        passenger2 = userRepository.save(passenger2);

        // Create tickets
        ticket1 = Ticket.builder()
                .trip(trip1)
                .passenger(passenger1)
                .fromStop(stop1)
                .toStop(stop2)
                .seatNumber("15")
                .price(new BigDecimal("75000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .passengerType(PassengerType.ADULT)
                .qrCode("QR-TICKET-001")
                .build();
        ticket1 = ticketRepository.save(ticket1);

        ticket2 = Ticket.builder()
                .trip(trip1)
                .passenger(passenger2)
                .fromStop(stop1)
                .toStop(stop2)
                .seatNumber("16")
                .price(new BigDecimal("75000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .passengerType(PassengerType.ADULT)
                .qrCode("QR-TICKET-002")
                .build();
        ticket2 = ticketRepository.save(ticket2);

        // Create baggages
        baggage1 = Baggage.builder()
                .ticket(ticket1)
                .weightKg(new BigDecimal("15.50"))
                .fee(new BigDecimal("5000"))
                .tagCode("BAG001")
                .build();

        baggage2 = Baggage.builder()
                .ticket(ticket1)
                .weightKg(new BigDecimal("23.00"))
                .fee(new BigDecimal("8000"))
                .tagCode("BAG002")
                .build();

        baggage3 = Baggage.builder()
                .ticket(ticket2)
                .weightKg(new BigDecimal("10.00"))
                .fee(new BigDecimal("3000"))
                .tagCode("BAG003")
                .build();
    }

    @Test
    @DisplayName("Baggage: find by weight greater than or equal")
    void shouldFindByWeightKgGreaterThanEqual() {
        // Given
        baggageRepository.save(baggage1);
        baggageRepository.save(baggage2);
        baggageRepository.save(baggage3);

        // When
        var result = baggageRepository.findByWeightKgGreaterThanEqual(
                new BigDecimal("15.00"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("BAG001", "BAG002");
    }

    @Test
    @DisplayName("Baggage: find by weight less than or equal")
    void shouldFindByWeightKgLessThanEqual() {
        // Given
        baggageRepository.save(baggage1);
        baggageRepository.save(baggage2);
        baggageRepository.save(baggage3);

        // When
        var result = baggageRepository.findByWeightKgLessThanEqual(
                new BigDecimal("15.50"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("BAG001", "BAG003");
    }

    @Test
    @DisplayName("Baggage: find by weight between")
    void shouldFindByWeightKgBetween() {
        // Given
        baggageRepository.save(baggage1);
        baggageRepository.save(baggage2);
        baggageRepository.save(baggage3);

        // When
        var result = baggageRepository.findByWeightKgBetween(
                new BigDecimal("10.00"),
                new BigDecimal("16.00"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("BAG001", "BAG003");
    }

    @Test
    @DisplayName("Baggage: find by tag code")
    void shouldFindByTagCode() {
        // Given
        baggageRepository.save(baggage1);
        baggageRepository.save(baggage2);

        // When
        var result = baggageRepository.findByTagCode("BAG001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getWeightKg()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(result.get().getFee()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("Baggage: find by passenger id")
    void shouldFindByPassengerId() {
        // Given
        baggageRepository.save(baggage1);
        baggageRepository.save(baggage2);
        baggageRepository.save(baggage3);

        // When
        var result = baggageRepository.findByTicket_Passenger_Id(passenger1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("BAG001", "BAG002");
    }

    @Test
    @DisplayName("Baggage: find all by ticket id")
    void shouldFindAllByTicketId() {
        // Given
        baggageRepository.save(baggage1);
        baggageRepository.save(baggage2);
        baggageRepository.save(baggage3);

        // When
        var result = baggageRepository.findAllByTicketId(ticket1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("BAG001", "BAG002");
    }

    @Test
    @DisplayName("Baggage: count by trip id")
    void shouldCountByTripId() {
        // Given
        baggageRepository.save(baggage1);
        baggageRepository.save(baggage2);
        baggageRepository.save(baggage3);

        // When
        var count = baggageRepository.countByTripId(trip1.getId());

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("Baggage: sum weight by trip id")
    void shouldSumWeightByTripId() {
        // Given
        baggageRepository.save(baggage1);
        baggageRepository.save(baggage2);
        baggageRepository.save(baggage3);

        // When
        var totalWeight = baggageRepository.sumWeightByTripId(trip1.getId());

        // Then
        // 15.50 + 23.00 + 10.00 = 48.50
        assertThat(totalWeight).isEqualByComparingTo(new BigDecimal("48.50"));
    }

    @Test
    @DisplayName("Baggage: return empty when tag code not found")
    void shouldReturnEmptyWhenTagCodeNotFound() {
        // Given
        baggageRepository.save(baggage1);

        // When
        var result = baggageRepository.findByTagCode("BAG999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Baggage: return empty list when passenger has no baggage")
    void shouldReturnEmptyWhenPassengerHasNoBaggage() {
        // Given - passenger without baggage
        User passenger3 = User.builder()
                .name("Carlos Test")
                .email("carlos@mail.com")
                .phone("3009876543")
                .passwordHash("hashed_pass_789")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();
        passenger3 = userRepository.save(passenger3);

        // When
        var result = baggageRepository.findByTicket_Passenger_Id(passenger3.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Baggage: return zero when trip has no baggage")
    void shouldReturnZeroWhenTripHasNoBaggage() {
        // Given - trip without baggage
        Trip trip2 = Trip.builder()
                .route(trip1.getRoute())
                .bus(trip1.getBus())
                .date(LocalDate.now().plusDays(3))
                .departureTime(OffsetDateTime.now().plusDays(3))
                .arrivalTime(OffsetDateTime.now().plusDays(3).plusHours(10))
                .status(TripStatus.SCHEDULED)
                .build();
        trip2 = tripRepository.save(trip2);

        // When
        var count = baggageRepository.countByTripId(trip2.getId());
        var totalWeight = baggageRepository.sumWeightByTripId(trip2.getId());

        // Then
        assertThat(count).isEqualTo(0L);
        assertThat(totalWeight).isEqualByComparingTo(BigDecimal.ZERO);
    }
}