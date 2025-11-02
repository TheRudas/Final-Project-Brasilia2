package co.edu.unimagdalena.finalproject_brasilia2.domain.entities;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(nullable = false, name = "seat_number")
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "seat_type")
    private SeatType seatType;
}
