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

    private Baggage lightBaggage;
    private Baggage mediumBaggage;
    private Baggage heavyBaggage;
    private User passenger;

    @BeforeEach
    void setUp() {
        baggageRepository.deleteAll();
        ticketRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();
        stopRepository.deleteAll();

        // Create passenger
        passenger = User.builder()
                .name("Cold Menares")
                .email("cold@mail.com")
                .phone("3001112222")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("")
                .createdAt(OffsetDateTime.now())
                .build();
        userRepository.save(passenger);

        // Create route
        Route route = Route.builder()
                .code("R001")
                .name("Bogotá-Cartagena")
                .origin("Bogotá")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("1000"))
                .durationMin(720)
                .build();
        routeRepository.save(route);

        // Create stops
        Stop fromStop = Stop.builder()
                .route(route)
                .name("Terminal Bogotá")
                .order(1)
                .build();
        stopRepository.save(fromStop);

        Stop toStop = Stop.builder()
                .route(route)
                .name("Terminal Cartagena")
                .order(2)
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
        Trip trip = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusHours(14))
                .status(TripStatus.SCHEDULED)
                .build();
        tripRepository.save(trip);

        // Create ticket
        Ticket ticket = Ticket.builder()
                .trip(trip)
                .passenger(passenger)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("150000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR123456")
                .build();
        ticketRepository.save(ticket);

        // Create some baggage, light beer, medium and fridges
        lightBaggage = Baggage.builder()
                .ticket(ticket)
                .weightKg(new BigDecimal("10.50"))
                .fee(new BigDecimal("15000"))
                .tagCode("TAG001")
                .build();

        mediumBaggage = Baggage.builder()
                .ticket(ticket)
                .weightKg(new BigDecimal("20.00"))
                .fee(new BigDecimal("25000"))
                .tagCode("TAG002")
                .build();

        heavyBaggage = Baggage.builder()
                .ticket(ticket)
                .weightKg(new BigDecimal("30.75"))
                .fee(new BigDecimal("35000"))
                .tagCode("TAG003")
                .build();
    }

    @Test
    @DisplayName("Baggage: find by weight greater than or equal")
    void shouldFindByWeightGreaterThanOrEqual() {
        // Given
        baggageRepository.save(lightBaggage);
        baggageRepository.save(mediumBaggage);
        baggageRepository.save(heavyBaggage);

        // When
        var result = baggageRepository.findByWeightKgGreaterThanEqual(
                new BigDecimal("20.00"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("TAG002", "TAG003");
    }

    @Test
    @DisplayName("Baggage: find by weight less than or equal")
    void shouldFindByWeightLessThanOrEqual() {
        // Given
        baggageRepository.save(lightBaggage);
        baggageRepository.save(mediumBaggage);
        baggageRepository.save(heavyBaggage);

        // When
        var result = baggageRepository.findByWeightKgLessThanEqual(
                new BigDecimal("20.00"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("TAG001", "TAG002");
    }

    @Test
    @DisplayName("Baggage: find by weight between range")
    void shouldFindByWeightBetween() {
        // Given
        baggageRepository.save(lightBaggage);
        baggageRepository.save(mediumBaggage);
        baggageRepository.save(heavyBaggage);

        // When
        var result = baggageRepository.findByWeightKgBetween(
                new BigDecimal("15.00"),
                new BigDecimal("25.00"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTagCode()).isEqualTo("TAG002");
    }

    @Test
    @DisplayName("Baggage: find by tag code")
    void shouldFindByTagCode() {
        // Given
        baggageRepository.save(mediumBaggage);

        // When
        var result = baggageRepository.findByTagCode("TAG002");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getWeightKg()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("Baggage: find by passenger id")
    void shouldFindByPassengerId() {
        // Given
        baggageRepository.save(lightBaggage);
        baggageRepository.save(mediumBaggage);
        baggageRepository.save(heavyBaggage);

        // When
        var result = baggageRepository.findByTicket_Passenger_Id(passenger.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("TAG001", "TAG002", "TAG003");
    }

    @Test
    @DisplayName("Baggage: return empty when tag code not found")
    void shouldReturnEmptyWhenTagCodeNotFound() {
        // Given
        baggageRepository.save(lightBaggage);

        // When
        var result = baggageRepository.findByTagCode("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Baggage: return empty page when no baggage matches weight criteria")
    void shouldReturnEmptyPageWhenNoMatch() {
        // Given
        baggageRepository.save(lightBaggage);

        // When
        var result = baggageRepository.findByWeightKgGreaterThanEqual(
                new BigDecimal("50.00"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).isEmpty();
    }
}