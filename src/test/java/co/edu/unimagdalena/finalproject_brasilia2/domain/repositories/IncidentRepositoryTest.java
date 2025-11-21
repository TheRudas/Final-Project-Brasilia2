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

    private Incident incident1;
    private Incident incident2;
    private Incident incident3;
    private Incident incident4;
    private Incident incident5;

    @BeforeEach
    void setUp() {
        incidentRepository.deleteAll();

        OffsetDateTime now = OffsetDateTime.now();

        // Create incidents
        incident1 = Incident.builder()
                .entityType(IncidentEntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.VEHICLE)
                .note("Bus had mechanical failure")
                .createdAt(now.minusDays(5))
                .build();

        incident2 = Incident.builder()
                .entityType(IncidentEntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.SECURITY)
                .note("Security incident on route")
                .createdAt(now.minusDays(3))
                .build();

        incident3 = Incident.builder()
                .entityType(IncidentEntityType.TICKET)
                .entityId(100L)
                .type(IncidentType.OVERBOOK)
                .note("Passenger complained about overbooking")
                .createdAt(now.minusDays(2))
                .build();

        incident4 = Incident.builder()
                .entityType(IncidentEntityType.PARCEL)
                .entityId(50L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Package not delivered to recipient")
                .createdAt(now.minusDays(1))
                .build();

        incident5 = Incident.builder()
                .entityType(IncidentEntityType.PARCEL)
                .entityId(51L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Wrong delivery address")
                .createdAt(now)
                .build();
    }

    @Test
    @DisplayName("Incident: find by entity type")
    void shouldFindByEntityType() {
        // Given
        incidentRepository.save(incident1);
        incidentRepository.save(incident2);
        incidentRepository.save(incident3);
        incidentRepository.save(incident4);

        // When
        var tripIncidents = incidentRepository.findByEntityType(IncidentEntityType.TRIP);
        var parcelIncidents = incidentRepository.findByEntityType(IncidentEntityType.PARCEL);

        // Then
        assertThat(tripIncidents).hasSize(2);
        assertThat(tripIncidents)
                .extracting(Incident::getEntityType)
                .containsOnly(IncidentEntityType.TRIP);

        assertThat(parcelIncidents).hasSize(1);
        assertThat(parcelIncidents.get(0).getEntityId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Incident: find by entity id")
    void shouldFindByEntityId() {
        // Given
        incidentRepository.save(incident1);
        incidentRepository.save(incident2);
        incidentRepository.save(incident3);

        // When
        var result = incidentRepository.findByEntityId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Incident::getType)
                .containsExactlyInAnyOrder(IncidentType.VEHICLE, IncidentType.SECURITY);
    }

    @Test
    @DisplayName("Incident: find by entity type and entity id")
    void shouldFindByEntityTypeAndEntityId() {
        // Given
        incidentRepository.save(incident1);
        incidentRepository.save(incident2);
        incidentRepository.save(incident3);

        // When
        var result = incidentRepository.findByEntityTypeAndEntityId(
                IncidentEntityType.TRIP,
                1L
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Incident::getNote)
                .containsExactlyInAnyOrder(
                        "Bus had mechanical failure",
                        "Security incident on route"
                );
    }

    @Test
    @DisplayName("Incident: find by type")
    void shouldFindByType() {
        // Given
        incidentRepository.save(incident1);
        incidentRepository.save(incident2);
        incidentRepository.save(incident3);
        incidentRepository.save(incident4);
        incidentRepository.save(incident5);

        // When
        var deliveryFailIncidents = incidentRepository.findByType(IncidentType.DELIVERY_FAIL);
        var vehicleIncidents = incidentRepository.findByType(IncidentType.VEHICLE);

        // Then
        assertThat(deliveryFailIncidents).hasSize(2);
        assertThat(deliveryFailIncidents)
                .extracting(Incident::getEntityType)
                .containsOnly(IncidentEntityType.PARCEL);

        assertThat(vehicleIncidents).hasSize(1);
        assertThat(vehicleIncidents.get(0).getNote()).isEqualTo("Bus had mechanical failure");
    }

    @Test
    @DisplayName("Incident: count by entity type")
    void shouldCountByEntityType() {
        // Given
        incidentRepository.save(incident1);
        incidentRepository.save(incident2);
        incidentRepository.save(incident3);
        incidentRepository.save(incident4);
        incidentRepository.save(incident5);

        // When
        var tripCount = incidentRepository.countByEntityType(IncidentEntityType.TRIP);
        var ticketCount = incidentRepository.countByEntityType(IncidentEntityType.TICKET);
        var parcelCount = incidentRepository.countByEntityType(IncidentEntityType.PARCEL);

        // Then
        assertThat(tripCount).isEqualTo(2L);
        assertThat(ticketCount).isEqualTo(1L);
        assertThat(parcelCount).isEqualTo(2L);
    }

    @Test
    @DisplayName("Incident: find by created at between dates")
    void shouldFindByCreatedAtBetween() {
        // Given
        incidentRepository.save(incident1);
        incidentRepository.save(incident2);
        incidentRepository.save(incident3);
        incidentRepository.save(incident4);
        incidentRepository.save(incident5);

        OffsetDateTime now = OffsetDateTime.now();
        // Rango inclusivo: desde hace 3 días hasta hace 1 día
        OffsetDateTime start = now.minusDays(3).minusMinutes(1); // Un minuto antes para incluir el límite
        OffsetDateTime end = now.minusDays(1).plusMinutes(1); // Un minuto después para incluir el límite

        // When
        var result = incidentRepository.findByCreatedAtBetween(start, end);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Incident::getNote)
                .containsExactlyInAnyOrder(
                        "Security incident on route",
                        "Passenger complained about overbooking",
                        "Package not delivered to recipient"
                );
    }

    @Test
    @DisplayName("Incident: return empty list when entity type has no incidents")
    void shouldReturnEmptyWhenEntityTypeHasNoIncidents() {
        // Given
        incidentRepository.save(incident1);

        // When
        var result = incidentRepository.findByEntityType(IncidentEntityType.TICKET);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Incident: return empty list when entity id has no incidents")
    void shouldReturnEmptyWhenEntityIdHasNoIncidents() {
        // Given
        incidentRepository.save(incident1);

        // When
        var result = incidentRepository.findByEntityId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Incident: return empty list when type has no incidents")
    void shouldReturnEmptyWhenTypeHasNoIncidents() {
        // Given
        incidentRepository.save(incident1);

        // When
        var result = incidentRepository.findByType(IncidentType.OVERBOOK);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Incident: return empty list when date range has no incidents")
    void shouldReturnEmptyWhenDateRangeHasNoIncidents() {
        // Given
        incidentRepository.save(incident1);

        OffsetDateTime start = OffsetDateTime.now().minusYears(2);
        OffsetDateTime end = OffsetDateTime.now().minusYears(1);

        // When
        var result = incidentRepository.findByCreatedAtBetween(start, end);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Incident: return zero count when entity type has no incidents")
    void shouldReturnZeroCountWhenEntityTypeHasNoIncidents() {
        // Given
        incidentRepository.save(incident1);

        // When
        var count = incidentRepository.countByEntityType(IncidentEntityType.TICKET);

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("Incident: verify all incident types can be saved")
    void shouldVerifyAllIncidentTypesCanBeSaved() {
        // Given
        Incident securityIncident = Incident.builder()
                .entityType(IncidentEntityType.TRIP)
                .entityId(10L)
                .type(IncidentType.SECURITY)
                .note("Security issue")
                .createdAt(OffsetDateTime.now())
                .build();

        Incident deliveryFailIncident = Incident.builder()
                .entityType(IncidentEntityType.PARCEL)
                .entityId(20L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Delivery failed")
                .createdAt(OffsetDateTime.now())
                .build();

        Incident overbookIncident = Incident.builder()
                .entityType(IncidentEntityType.TICKET)
                .entityId(30L)
                .type(IncidentType.OVERBOOK)
                .note("Overbooked")
                .createdAt(OffsetDateTime.now())
                .build();

        Incident vehicleIncident = Incident.builder()
                .entityType(IncidentEntityType.TRIP)
                .entityId(40L)
                .type(IncidentType.VEHICLE)
                .note("Vehicle problem")
                .createdAt(OffsetDateTime.now())
                .build();

        // When
        incidentRepository.save(securityIncident);
        incidentRepository.save(deliveryFailIncident);
        incidentRepository.save(overbookIncident);
        incidentRepository.save(vehicleIncident);

        // Then
        assertThat(incidentRepository.findByType(IncidentType.SECURITY)).hasSize(1);
        assertThat(incidentRepository.findByType(IncidentType.DELIVERY_FAIL)).hasSize(1);
        assertThat(incidentRepository.findByType(IncidentType.OVERBOOK)).hasSize(1);
        assertThat(incidentRepository.findByType(IncidentType.VEHICLE)).hasSize(1);
    }
}