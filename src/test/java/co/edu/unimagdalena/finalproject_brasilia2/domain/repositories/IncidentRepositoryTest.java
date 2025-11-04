package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Incident;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class IncidentRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private IncidentRepository incidentRepository;

    private Incident tripIncident1;
    private Incident tripIncident2;
    private Incident ticketIncident;
    private Incident parcelIncident;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        incidentRepository.deleteAll();
        baseTime = OffsetDateTime.now();

        tripIncident1 = Incident.builder()
                .entityType(IncidentEntityType.TRIP)
                .entityId(100L)
                .type(IncidentType.VEHICLE)
                .note("A child flew over me and blew up a car with his laser beam")
                .createdAt(baseTime.minusHours(5))
                .build();

        tripIncident2 = Incident.builder()
                .entityType(IncidentEntityType.TRIP)
                .entityId(101L)
                .type(IncidentType.SECURITY)
                .note("Security check required")
                .createdAt(baseTime.minusHours(3))
                .build();

        ticketIncident = Incident.builder()
                .entityType(IncidentEntityType.TICKET)
                .entityId(200L)
                .type(IncidentType.OVERBOOK)
                .note("Ticket overbooked")
                .createdAt(baseTime.minusHours(2))
                .build();

        parcelIncident = Incident.builder()
                .entityType(IncidentEntityType.PARCEL)
                .entityId(300L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Delivery failed - Sincelejo is a government invention")
                .createdAt(baseTime.minusHours(1))
                .build();
    }

    @Test
    @DisplayName("Incident: find by entity type")
    void shouldFindByEntityType() {
        // Given
        incidentRepository.save(tripIncident1);
        incidentRepository.save(tripIncident2);
        incidentRepository.save(ticketIncident);
        incidentRepository.save(parcelIncident);

        // When
        var tripIncidents = incidentRepository.findByEntityType(IncidentEntityType.TRIP);
        var ticketIncidents = incidentRepository.findByEntityType(IncidentEntityType.TICKET);

        // Then
        assertThat(tripIncidents).hasSize(2);
        assertThat(tripIncidents)
                .extracting(Incident::getEntityId)
                .containsExactlyInAnyOrder(100L, 101L);

        assertThat(ticketIncidents).hasSize(1);
        assertThat(ticketIncidents.get(0).getEntityId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("Incident: find by entity id")
    void shouldFindByEntityId() {
        // Given
        incidentRepository.save(tripIncident1);
        incidentRepository.save(tripIncident2);

        // When
        var result = incidentRepository.findByEntityId(100L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNote()).isEqualTo("A child flew over me and blew up a car with his laser beam");
    }

    @Test
    @DisplayName("Incident: find by entity type and entity id")
    void shouldFindByEntityTypeAndEntityId() {
        // Given
        incidentRepository.save(tripIncident1);
        incidentRepository.save(tripIncident2);
        incidentRepository.save(ticketIncident);

        // When
        var result = incidentRepository.findByEntityTypeAndEntityId(
                IncidentEntityType.TRIP,
                100L
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(IncidentType.VEHICLE);
        assertThat(result.get(0).getNote()).isEqualTo("A child flew over me and blew up a car with his laser beam");
    }

    @Test
    @DisplayName("Incident: find by incident type")
    void shouldFindByType() {
        // Given
        incidentRepository.save(tripIncident1);
        incidentRepository.save(tripIncident2);
        incidentRepository.save(ticketIncident);
        incidentRepository.save(parcelIncident);

        // When
        var vehicleIncidents = incidentRepository.findByType(IncidentType.VEHICLE);
        var securityIncidents = incidentRepository.findByType(IncidentType.SECURITY);

        // Then
        assertThat(vehicleIncidents).hasSize(1);
        assertThat(vehicleIncidents.get(0).getNote()).isEqualTo("A child flew over me and blew up a car with his laser beam");

        assertThat(securityIncidents).hasSize(1);
        assertThat(securityIncidents.get(0).getNote()).isEqualTo("Security check required");
    }

    @Test
    @DisplayName("Incident: count by entity type")
    void shouldCountByEntityType() {
        // Given
        incidentRepository.save(tripIncident1);
        incidentRepository.save(tripIncident2);
        incidentRepository.save(ticketIncident);
        incidentRepository.save(parcelIncident);

        // When
        var tripCount = incidentRepository.countByEntityType(IncidentEntityType.TRIP);
        var ticketCount = incidentRepository.countByEntityType(IncidentEntityType.TICKET);
        var parcelCount = incidentRepository.countByEntityType(IncidentEntityType.PARCEL);

        // Then
        assertThat(tripCount).isEqualTo(2);
        assertThat(ticketCount).isEqualTo(1);
        assertThat(parcelCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Incident: find by created at between dates")
    void shouldFindByCreatedAtBetween() {
        // Given
        incidentRepository.save(tripIncident1);
        incidentRepository.save(tripIncident2);
        incidentRepository.save(ticketIncident);
        incidentRepository.save(parcelIncident);

        // When
        var result = incidentRepository.findByCreatedAtBetween(
                baseTime.minusHours(4),
                baseTime.minusMinutes(90)
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Incident::getNote)
                .containsExactlyInAnyOrder("Security check required", "Ticket overbooked");
    }

    @Test
    @DisplayName("Incident: return empty list when entity type has no incidents")
    void shouldReturnEmptyWhenEntityTypeNotFound() {
        // Given
        incidentRepository.save(tripIncident1);

        // When
        var result = incidentRepository.findByEntityType(IncidentEntityType.PARCEL);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Incident: return empty list when entity id has no incidents")
    void shouldReturnEmptyWhenEntityIdNotFound() {
        // Given
        incidentRepository.save(tripIncident1);

        // When
        var result = incidentRepository.findByEntityId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Incident: return zero count when entity type has no incidents")
    void shouldReturnZeroCountWhenNoIncidents() {
        // Given - no incidents saved

        // When
        var count = incidentRepository.countByEntityType(IncidentEntityType.TRIP);

        // Then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Incident: return empty list when no incidents in date range")
    void shouldReturnEmptyWhenNoIncidentsInRange() {
        // Given
        incidentRepository.save(tripIncident1);

        // When
        var result = incidentRepository.findByCreatedAtBetween(
                baseTime.plusHours(1),
                baseTime.plusHours(2)
        );

        // Then
        assertThat(result).isEmpty();
    }
}