package co.edu.unimagdalena.finalproject_brasilia2.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Cash register closure
 * Tracks daily cash collections by clerks
 */
@Entity
@Table(name = "cash_closures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clerk_id", nullable = false)
    private User clerk;

    @Column(name = "closure_date", nullable = false)
    private LocalDate closureDate;

    @Column(name = "total_cash", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCash;

    @Column(name = "total_tickets_sold", nullable = false)
    private Integer totalTicketsSold;

    @Column(name = "total_parcels_registered", nullable = false)
    private Integer totalParcelsRegistered;

    @Column(name = "total_baggage_fees", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalBaggageFees;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}