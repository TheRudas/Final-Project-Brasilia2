package co.edu.unimagdalena.finalproject_brasilia2.domain.entities;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Payment entity
 * Tracks payment transactions for tickets
 * Payment state is independent from Ticket state
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    /**
     * Payment confirmation status
     * true = confirmed and ticket is SOLD
     * false = pending confirmation (QR/Transfer)
     */
    @Column(nullable = false)
    private boolean confirmed = false;

    @Column(name = "transaction_id", unique = true, length = 100, nullable = false)
    private String transactionId;

    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    @Column(name = "payment_proof_url", length = 255)
    private String paymentProofUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    /**
     * Confirm payment
     */
    public void confirm() {
        this.confirmed = true;
        this.confirmedAt = OffsetDateTime.now();
    }

    /**
     * Mark payment as failed
     */
    public void markAsFailed(String reason) {
        this.failureReason = reason;
    }

    /**
     * Check if payment is pending
     */
    public boolean isPending() {
        return !confirmed && failureReason == null;
    }

    /**
     * Check if payment failed
     */
    public boolean isFailed() {
        return failureReason != null;
    }
}