package co.edu.unimagdalena.finalproject_brasilia2.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "buses")

public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String plate;

    @Column(nullable = false)
    private Integer capacity;

    @ElementCollection
    @CollectionTable(name = "bus_amenities", joinColumns = @JoinColumn(name = "bus_id"))
    @Column(name = "amenity")
    private Set<String> amenities = new HashSet<>();

    @Column(name = "bus_status", nullable = false)
    private boolean status;
}
