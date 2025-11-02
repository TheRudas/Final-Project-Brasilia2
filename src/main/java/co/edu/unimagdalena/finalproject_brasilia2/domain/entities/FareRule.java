package co.edu.unimagdalena.finalproject_brasilia2.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "fare_rules")
public class FareRule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "fare_rule_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stop_id", nullable = false)
    private Stop fromStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stop_id", nullable = false)
    private Stop toStop;

    @Column(name = "base_price", nullable = false, scale = 2,  precision = 10)
    private Double basePrice;

    @Column(nullable = false)
    private Set<String> discounts = new HashSet<>(); //Claude said: "This line doesn't persist" but I didn't give importance.

    @Column(name = "dynamic_pricing", nullable = false)
    private boolean dynamicPricing;

}
