package co.edu.unimagdalena.finalproject_brasilia2.domain.entities;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PassengerType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stop_id", nullable = false)
    private Stop fromStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stop_id", nullable = false)
    private Stop toStop;

    @Column(nullable = false, scale = 2, precision = 10)
    private BigDecimal price;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "ticket_status")
    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Column(name = "qrCode", nullable = false, unique = true)
    private String qrCode;

    @Column(name = "no_show_fee", scale = 2, precision = 10)
    private BigDecimal noShowFee;

    @Column(name = "refund_amount", scale = 2, precision = 10)
    private BigDecimal refundAmount;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type")
    private PassengerType passengerType;
}
