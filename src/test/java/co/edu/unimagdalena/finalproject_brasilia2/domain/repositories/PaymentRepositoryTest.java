package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Payment Repository Tests")
class PaymentRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Payment testPayment;
    private Ticket testTicket;
    private User testUser;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        // Crear usuario
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("password123")
                .phone("1234567890")
                .role(UserRole.PASSENGER)
                .status(false)
                .createdAt(OffsetDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Crear ruta
        Route route = Route.builder()
                .name("Test Route")
                .code("TR-001")
                .origin("City A")
                .destination("City B")
                .distanceKm(java.math.BigDecimal.valueOf(100))
                .durationMin(120)
                .build();
        route = routeRepository.save(route);

        // Crear paradas
        Stop fromStop = Stop.builder()
                .route(route)
                .name("Stop A")
                .order(1)
                .build();
        fromStop = stopRepository.save(fromStop);

        Stop toStop = Stop.builder()
                .route(route)
                .name("Stop B")
                .order(2)
                .build();
        toStop = stopRepository.save(toStop);

        // Crear bus
        Bus bus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();
        bus = busRepository.save(bus);

        // Crear viaje
        testTrip = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now())
                .arrivalTime(OffsetDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();
        testTrip = tripRepository.save(testTrip);

        // Crear ticket
        testTicket = Ticket.builder()
                .trip(testTrip)
                .passenger(testUser)
                .seatNumber("A1")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-TEST-123")
                .build();
        testTicket = ticketRepository.save(testTicket);

        // Crear pago
        testPayment = Payment.builder()
                .ticket(testTicket)
                .amount(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN-123456")
                .paymentReference("REF-789")
                .notes("Test payment")
                .build();
        testPayment = paymentRepository.save(testPayment);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find payment by ticket id")
    void shouldFindPaymentByTicketId() {
        Optional<Payment> found = paymentRepository.findByTicketId(testTicket.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTicket().getId()).isEqualTo(testTicket.getId());
        assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should find payment by transaction id")
    void shouldFindPaymentByTransactionId() {
        Optional<Payment> found = paymentRepository.findByTransactionId("TXN-123456");

        assertThat(found).isPresent();
        assertThat(found.get().getTransactionId()).isEqualTo("TXN-123456");
        assertThat(found.get().getPaymentReference()).isEqualTo("REF-789");
    }

    @Test
    @DisplayName("Should find payments by payment method")
    void shouldFindPaymentsByPaymentMethod() {
        Page<Payment> payments = paymentRepository.findByPaymentMethod(
                PaymentMethod.CARD, PageRequest.of(0, 10));

        assertThat(payments).isNotEmpty();
        assertThat(payments.getContent().getFirst().getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
    }

    @Test
    @DisplayName("Should find payments by status")
    void shouldFindPaymentsByStatus() {
        Page<Payment> payments = paymentRepository.findByStatus(
                PaymentStatus.COMPLETED, PageRequest.of(0, 10));

        assertThat(payments).isNotEmpty();
        assertThat(payments.getContent().getFirst().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should find payments by date range")
    void shouldFindPaymentsByDateRange() {
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(1);

        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);

        assertThat(payments).isNotEmpty();
        assertThat(payments.getFirst().getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should find payments by passenger id")
    void shouldFindPaymentsByPassengerId() {
        List<Payment> payments = paymentRepository.findByPassengerId(testUser.getId());

        assertThat(payments).isNotEmpty();
        assertThat(payments.getFirst().getTicket().getPassenger().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should save payment successfully")
    void shouldSavePaymentSuccessfully() {
        // Crear otro ticket para el nuevo pago
        Ticket newTicket = Ticket.builder()
                .trip(testTrip)
                .passenger(testUser)
                .seatNumber("B1")
                .fromStop(testTicket.getFromStop())
                .toStop(testTicket.getToStop())
                .price(new BigDecimal("60000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR-TEST-456")
                .build();
        newTicket = ticketRepository.save(newTicket);

        Payment newPayment = Payment.builder()
                .ticket(newTicket)
                .amount(new BigDecimal("60000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .transactionId("TXN-789012")
                .build();

        Payment saved = paymentRepository.save(newPayment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
    }

    @Test
    @DisplayName("Should update payment status")
    void shouldUpdatePaymentStatus() {
        testPayment.setStatus(PaymentStatus.REFUNDED);
        Payment updated = paymentRepository.save(testPayment);

        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("Should delete payment")
    void shouldDeletePayment() {
        Long paymentId = testPayment.getId();
        paymentRepository.delete(testPayment);

        Optional<Payment> found = paymentRepository.findById(paymentId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when payment not found by ticket id")
    void shouldReturnEmptyWhenPaymentNotFoundByTicketId() {
        Optional<Payment> found = paymentRepository.findByTicketId(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when payment not found by transaction id")
    void shouldReturnEmptyWhenPaymentNotFoundByTransactionId() {
        Optional<Payment> found = paymentRepository.findByTransactionId("INVALID-TXN");
        assertThat(found).isEmpty();
    }
}

