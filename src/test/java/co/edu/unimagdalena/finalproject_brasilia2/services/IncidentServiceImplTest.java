package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Incident;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.IncidentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.IncidentServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.IncidentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Spy
    private IncidentMapper mapper = Mappers.getMapper(IncidentMapper.class);

    @InjectMocks
    private IncidentServiceImpl service;

    @Test
    void shouldCreateAndReturnResponse() {
        // Given
        var request = new IncidentCreateRequest(
                IncidentEntityType.TRIP,
                100L,
                IncidentType.OVERBOOK,
                "Trip overbooked, need to reassign passengers"
        );

        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident incident = inv.getArgument(0);
            incident.setId(1L);
            return incident;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.entityType()).isEqualTo(IncidentEntityType.TRIP);
        assertThat(response.entityId()).isEqualTo(100L);
        assertThat(response.type()).isEqualTo(IncidentType.OVERBOOK);
        assertThat(response.note()).isEqualTo("Trip overbooked, need to reassign passengers");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.createdAt()).isBeforeOrEqualTo(OffsetDateTime.now());

        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldSetCreatedAtWhenCreating() {
        // Given
        var request = new IncidentCreateRequest(
                IncidentEntityType.PARCEL,
                200L,
                IncidentType.DELIVERY_FAIL,
                "Parcel could not be delivered to destination"
        );

        var beforeCreation = OffsetDateTime.now();

        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident incident = inv.getArgument(0);
            incident.setId(2L);
            assertThat(incident.getCreatedAt()).isNotNull();
            assertThat(incident.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
            return incident;
        });

        // When
        service.create(request);

        // Then
        verify(incidentRepository).save(argThat(incident ->
                incident.getCreatedAt() != null &&
                        !incident.getCreatedAt().isBefore(beforeCreation)
        ));
    }

    @Test
    void shouldGetIncidentById() {
        // Given
        var incident = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.TICKET)
                .entityId(50L)
                .type(IncidentType.SECURITY)
                .note("Security issue detected with ticket")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        // When
        var response = service.get(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.entityType()).isEqualTo(IncidentEntityType.TICKET);
        assertThat(response.entityId()).isEqualTo(50L);
        assertThat(response.type()).isEqualTo(IncidentType.SECURITY);
        assertThat(response.note()).isEqualTo("Security issue detected with ticket");
        assertThat(response.createdAt()).isNotNull();

        verify(incidentRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentIncident() {
        // Given
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incident 99 not found");

        verify(incidentRepository).findById(99L);
    }

    @Test
    void shouldUpdateIncidentViaPatch() {
        // Given
        var incident = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(100L)
                .type(IncidentType.VEHICLE)
                .note("Original note")
                .createdAt(OffsetDateTime.now().minusDays(1))
                .build();

        var updateRequest = new IncidentUpdateRequest(
                "Updated note with more details",
                IncidentType.SECURITY
        );

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.note()).isEqualTo("Updated note with more details");
        assertThat(response.type()).isEqualTo(IncidentType.SECURITY);
        assertThat(response.id()).isEqualTo(1L);

        verify(incidentRepository).findById(1L);
        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldUpdateOnlyNoteWhenTypeIsNull() {
        // Given
        var incident = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.PARCEL)
                .entityId(200L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Original note")
                .createdAt(OffsetDateTime.now().minusDays(1))
                .build();

        var updateRequest = new IncidentUpdateRequest("Only note updated", null);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.note()).isEqualTo("Only note updated");
        assertThat(response.type()).isEqualTo(IncidentType.DELIVERY_FAIL); // Should remain unchanged

        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentIncident() {
        // Given
        var updateRequest = new IncidentUpdateRequest("New note", IncidentType.VEHICLE);
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incident 99 not found");

        verify(incidentRepository).findById(99L);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteExistingIncident() {
        // Given
        var incident = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(100L)
                .type(IncidentType.OVERBOOK)
                .note("Test incident")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        doNothing().when(incidentRepository).delete(incident);

        // When
        service.delete(1L);

        // Then
        verify(incidentRepository).findById(1L);
        verify(incidentRepository).delete(incident);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentIncident() {
        // Given
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incident 99 not found or it was deleted yet");

        verify(incidentRepository).findById(99L);
        verify(incidentRepository, never()).delete(any());
    }

    @Test
    void shouldGetIncidentsByEntityType() {
        // Given
        var incident1 = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(100L)
                .type(IncidentType.VEHICLE)
                .note("Trip 1 vehicle issue")
                .createdAt(OffsetDateTime.now())
                .build();

        var incident2 = Incident.builder()
                .id(2L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(101L)
                .type(IncidentType.OVERBOOK)
                .note("Trip 2 overbooked")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findByEntityType(IncidentEntityType.TRIP))
                .thenReturn(List.of(incident1, incident2));

        // When
        var result = service.listByEntityType(IncidentEntityType.TRIP);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).entityType()).isEqualTo(IncidentEntityType.TRIP);
        assertThat(result.get(0).entityId()).isEqualTo(100L);
        assertThat(result.get(1).entityType()).isEqualTo(IncidentEntityType.TRIP);
        assertThat(result.get(1).entityId()).isEqualTo(101L);

        verify(incidentRepository).findByEntityType(IncidentEntityType.TRIP);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoIncidentsForEntityType() {
        // Given
        when(incidentRepository.findByEntityType(IncidentEntityType.TICKET))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByEntityType(IncidentEntityType.TICKET))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No incidents found for entity type: TICKET");

        verify(incidentRepository).findByEntityType(IncidentEntityType.TICKET);
    }

    @Test
    void shouldGetIncidentsByEntityId() {
        // Given
        var incident1 = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.PARCEL)
                .entityId(50L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Parcel delivery failed")
                .createdAt(OffsetDateTime.now())
                .build();

        var incident2 = Incident.builder()
                .id(2L)
                .entityType(IncidentEntityType.PARCEL)
                .entityId(50L)
                .type(IncidentType.SECURITY)
                .note("Parcel security check failed")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findByEntityId(50L)).thenReturn(List.of(incident1, incident2));

        // When
        var result = service.listByEntityId(50L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).entityId()).isEqualTo(50L);
        assertThat(result.get(1).entityId()).isEqualTo(50L);

        verify(incidentRepository).findByEntityId(50L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoIncidentsForEntityId() {
        // Given
        when(incidentRepository.findByEntityId(999L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByEntityId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No incidents found for entity Id: 999");

        verify(incidentRepository).findByEntityId(999L);
    }

    @Test
    void shouldGetIncidentsByEntityTypeAndEntityId() {
        // Given
        var incident = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(100L)
                .type(IncidentType.VEHICLE)
                .note("Specific trip vehicle incident")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findByEntityTypeAndEntityId(IncidentEntityType.TRIP, 100L))
                .thenReturn(List.of(incident));

        // When
        var result = service.listByEntityTypeAndEntityId(IncidentEntityType.TRIP, 100L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().entityType()).isEqualTo(IncidentEntityType.TRIP);
        assertThat(result.getFirst().entityId()).isEqualTo(100L);

        verify(incidentRepository).findByEntityTypeAndEntityId(IncidentEntityType.TRIP, 100L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoIncidentsForEntityTypeAndEntityId() {
        // Given
        when(incidentRepository.findByEntityTypeAndEntityId(IncidentEntityType.TICKET, 500L))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByEntityTypeAndEntityId(IncidentEntityType.TICKET, 500L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No incidents found for TICKET and entity Id: 500");

        verify(incidentRepository).findByEntityTypeAndEntityId(IncidentEntityType.TICKET, 500L);
    }

    @Test
    void shouldGetIncidentsByType() {
        // Given
        var incident1 = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(100L)
                .type(IncidentType.VEHICLE)
                .note("Vehicle incident 1")
                .createdAt(OffsetDateTime.now())
                .build();

        var incident2 = Incident.builder()
                .id(2L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(101L)
                .type(IncidentType.VEHICLE)
                .note("Vehicle incident 2")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findByType(IncidentType.VEHICLE))
                .thenReturn(List.of(incident1, incident2));

        // When
        var result = service.listByType(IncidentType.VEHICLE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(IncidentType.VEHICLE);
        assertThat(result.get(1).type()).isEqualTo(IncidentType.VEHICLE);

        verify(incidentRepository).findByType(IncidentType.VEHICLE);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoIncidentsForType() {
        // Given
        when(incidentRepository.findByType(IncidentType.SECURITY))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByType(IncidentType.SECURITY))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incidents of SECURITY type not found");

        verify(incidentRepository).findByType(IncidentType.SECURITY);
    }

    @Test
    void shouldCountIncidentsByEntityType() {
        // Given
        when(incidentRepository.countByEntityType(IncidentEntityType.TRIP))
                .thenReturn(15L);

        // When
        var count = service.countByEntityType(IncidentEntityType.TRIP);

        // Then
        assertThat(count).isEqualTo(15L);

        verify(incidentRepository).countByEntityType(IncidentEntityType.TRIP);
    }

    @Test
    void shouldReturnZeroWhenNoIncidentsToCount() {
        // Given
        when(incidentRepository.countByEntityType(IncidentEntityType.PARCEL))
                .thenReturn(0L);

        // When
        var count = service.countByEntityType(IncidentEntityType.PARCEL);

        // Then
        assertThat(count).isZero();

        verify(incidentRepository).countByEntityType(IncidentEntityType.PARCEL);
    }

    @Test
    void shouldGetIncidentsByCreatedAtBetween() {
        // Given
        var start = OffsetDateTime.now().minusDays(7);
        var end = OffsetDateTime.now();

        var incident1 = Incident.builder()
                .id(1L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(100L)
                .type(IncidentType.VEHICLE)
                .note("Recent incident 1")
                .createdAt(OffsetDateTime.now().minusDays(3))
                .build();

        var incident2 = Incident.builder()
                .id(2L)
                .entityType(IncidentEntityType.PARCEL)
                .entityId(200L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Recent incident 2")
                .createdAt(OffsetDateTime.now().minusDays(1))
                .build();

        when(incidentRepository.findByCreatedAtBetween(start, end))
                .thenReturn(List.of(incident1, incident2));

        // When
        var result = service.listByCreatedAtBetween(start, end);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).createdAt()).isBetween(start, end);
        assertThat(result.get(1).createdAt()).isBetween(start, end);

        verify(incidentRepository).findByCreatedAtBetween(start, end);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoIncidentsInDateRange() {
        // Given
        var start = OffsetDateTime.now().minusYears(2);
        var end = OffsetDateTime.now().minusYears(1);

        when(incidentRepository.findByCreatedAtBetween(start, end))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByCreatedAtBetween(start, end))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No incidents found between")
                .hasMessageContaining(start.toString())
                .hasMessageContaining(end.toString());

        verify(incidentRepository).findByCreatedAtBetween(start, end);
    }

    @Test
    void shouldHandleMultipleIncidentsWithSameEntityType() {
        // Given
        var incidents = List.of(
                Incident.builder().id(1L).entityType(IncidentEntityType.TRIP).entityId(1L)
                        .type(IncidentType.VEHICLE).note("Note 1").createdAt(OffsetDateTime.now()).build(),
                Incident.builder().id(2L).entityType(IncidentEntityType.TRIP).entityId(2L)
                        .type(IncidentType.OVERBOOK).note("Note 2").createdAt(OffsetDateTime.now()).build(),
                Incident.builder().id(3L).entityType(IncidentEntityType.TRIP).entityId(3L)
                        .type(IncidentType.VEHICLE).note("Note 3").createdAt(OffsetDateTime.now()).build()
        );

        when(incidentRepository.findByEntityType(IncidentEntityType.TRIP))
                .thenReturn(incidents);

        // When
        var result = service.listByEntityType(IncidentEntityType.TRIP);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(r -> r.entityType() == IncidentEntityType.TRIP);
    }

    @Test
    void shouldCreateIncidentForEachEntityType() {
        // Given - TRIP
        var tripRequest = new IncidentCreateRequest(
                IncidentEntityType.TRIP, 1L, IncidentType.VEHICLE, "Trip vehicle issue"
        );
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        // When
        var tripResponse = service.create(tripRequest);

        // Then
        assertThat(tripResponse.entityType()).isEqualTo(IncidentEntityType.TRIP);

        // Given - TICKET
        var ticketRequest = new IncidentCreateRequest(
                IncidentEntityType.TICKET, 2L, IncidentType.SECURITY, "Ticket security issue"
        );

        // When
        var ticketResponse = service.create(ticketRequest);

        // Then
        assertThat(ticketResponse.entityType()).isEqualTo(IncidentEntityType.TICKET);

        // Given - PARCEL
        var parcelRequest = new IncidentCreateRequest(
                IncidentEntityType.PARCEL, 3L, IncidentType.DELIVERY_FAIL, "Parcel delivery failed"
        );

        // When
        var parcelResponse = service.create(parcelRequest);

        // Then
        assertThat(parcelResponse.entityType()).isEqualTo(IncidentEntityType.PARCEL);

        verify(incidentRepository, times(3)).save(any(Incident.class));
    }

    @Test
    void shouldCreateIncidentForEachIncidentType() {
        // Given
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        // SECURITY
        var securityResponse = service.create(new IncidentCreateRequest(
                IncidentEntityType.TRIP, 1L, IncidentType.SECURITY, "Security incident"
        ));
        assertThat(securityResponse.type()).isEqualTo(IncidentType.SECURITY);

        // DELIVERY_FAIL
        var deliveryResponse = service.create(new IncidentCreateRequest(
                IncidentEntityType.PARCEL, 2L, IncidentType.DELIVERY_FAIL, "Delivery failed"
        ));
        assertThat(deliveryResponse.type()).isEqualTo(IncidentType.DELIVERY_FAIL);

        // OVERBOOK
        var overbookResponse = service.create(new IncidentCreateRequest(
                IncidentEntityType.TRIP, 3L, IncidentType.OVERBOOK, "Overbooked"
        ));
        assertThat(overbookResponse.type()).isEqualTo(IncidentType.OVERBOOK);

        // VEHICLE
        var vehicleResponse = service.create(new IncidentCreateRequest(
                IncidentEntityType.TRIP, 4L, IncidentType.VEHICLE, "Vehicle issue"
        ));
        assertThat(vehicleResponse.type()).isEqualTo(IncidentType.VEHICLE);

        verify(incidentRepository, times(4)).save(any(Incident.class));
    }
}