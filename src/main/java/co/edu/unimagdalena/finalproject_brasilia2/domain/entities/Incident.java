package co.edu.unimagdalena.finalproject_brasilia2.domain.entities;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type",nullable = false)
    private IncidentEntityType entityType;

    @Column(name = "entity_id",nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type",nullable = false)
    private IncidentType type;

    @Column(name = "incident_note",nullable = false)
    private String note;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
