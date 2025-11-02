package co.edu.unimagdalena.finalproject_brasilia2.domain.entities;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "trips")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(name = "date",nullable = false)
    private LocalDate date; //Only saves trip's day, not specific hour or time

    @Column(name = "departure_time", nullable = false)
    private OffsetDateTime departureAt;

    @Column(name = "estimated_arrival_time", nullable = false)
    private OffsetDateTime arrivalEta;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TripStatus status;

}
