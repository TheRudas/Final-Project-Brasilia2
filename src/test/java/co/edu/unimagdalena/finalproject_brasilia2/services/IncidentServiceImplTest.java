package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.IncidentCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.IncidentUpdateRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void shouldCreateIncidentSuccessfully() {
        // Given
        var request = new IncidentCreateRequest(
                IncidentEntityType.TRIP,
                10L,
                IncidentType.SECURITY,
                "Suspicious package found on bus"
        );

        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.entityType()).isEqualTo(IncidentEntityType.TRIP);
        assertThat(response.entityId()).isEqualTo(10L);
        assertThat(response.type()).isEqualTo(IncidentType.SECURITY);
        assertThat(response.note()).isEqualTo("Suspicious package found on bus");
        assertThat(response.createdAt()).isNotNull();

        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldUpdateIncidentSuccessfully() {
        // Given
        var existingIncident = Incident.builder()
                .id(100L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(10L)
                .type(IncidentType.SECURITY)
                .note("Old note")
                .createdAt(OffsetDateTime.now())
                .build();

        var updateRequest = new IncidentUpdateRequest(
                "Updated note: delivery failed",
                IncidentType.DELIVERY_FAIL
        );

        when(incidentRepository.findById(100L)).thenReturn(Optional.of(existingIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(100L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.type()).isEqualTo(IncidentType.DELIVERY_FAIL);
        assertThat(response.note()).isEqualTo("Updated note: delivery failed");

        verify(incidentRepository).findById(100L);
        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentIncident() {
        // Given
        var updateRequest = new IncidentUpdateRequest(
                "Updated note",
                IncidentType.DELIVERY_FAIL
        );

        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incident 99 not found");

        verify(incidentRepository).findById(99L);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void shouldGetIncidentById() {
        // Given
        var incident = Incident.builder()
                .id(100L)
                .entityType(IncidentEntityType.TICKET)
                .entityId(5L)
                .type(IncidentType.OVERBOOK)
                .note("Overbooking detected")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        // When
        var response = service.get(100L);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.entityType()).isEqualTo(IncidentEntityType.TICKET);
        assertThat(response.type()).isEqualTo(IncidentType.OVERBOOK);

        verify(incidentRepository).findById(100L);
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
    void shouldDeleteIncidentSuccessfully() {
        // Given
        var incident = Incident.builder()
                .id(100L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(10L)
                .type(IncidentType.VEHICLE)
                .note("Engine failure")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        doNothing().when(incidentRepository).delete(incident);

        // When
        service.delete(100L);

        // Then
        verify(incidentRepository).findById(100L);
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
    void shouldListIncidentsByEntityType() {
        // Given
        var incident1 = Incident.builder()
                .id(100L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(10L)
                .type(IncidentType.SECURITY)
                .note("Security issue")
                .createdAt(OffsetDateTime.now())
                .build();

        var incident2 = Incident.builder()
                .id(101L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(11L)
                .type(IncidentType.VEHICLE)
                .note("Vehicle breakdown")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findByEntityType(IncidentEntityType.TRIP))
                .thenReturn(List.of(incident1, incident2));

        // When
        var result = service.listByEntityType(IncidentEntityType.TRIP);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().entityType()).isEqualTo(IncidentEntityType.TRIP);
        assertThat(result.get(1).entityType()).isEqualTo(IncidentEntityType.TRIP);

        verify(incidentRepository).findByEntityType(IncidentEntityType.TRIP);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoIncidentsForEntityType() {
        // Given
        when(incidentRepository.findByEntityType(IncidentEntityType.PARCEL))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByEntityType(IncidentEntityType.PARCEL))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No incidents found for entity type: PARCEL");

        verify(incidentRepository).findByEntityType(IncidentEntityType.PARCEL);
    }

    @Test
    void shouldListIncidentsByEntityId() {
        // Given
        var incident = Incident.builder()
                .id(100L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(10L)
                .type(IncidentType.SECURITY)
                .note("Security issue")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findByEntityId(10L))
                .thenReturn(List.of(incident));

        // When
        var result = service.listByEntityId(10L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().entityId()).isEqualTo(10L);

        verify(incidentRepository).findByEntityId(10L);
    }

    @Test
    void shouldListIncidentsByType() {
        // Given
        var incident = Incident.builder()
                .id(100L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(10L)
                .type(IncidentType.SECURITY)
                .note("Security incident")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentRepository.findByType(IncidentType.SECURITY))
                .thenReturn(List.of(incident));

        // When
        var result = service.listByType(IncidentType.SECURITY);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().type()).isEqualTo(IncidentType.SECURITY);

        verify(incidentRepository).findByType(IncidentType.SECURITY);
    }

    @Test
    void shouldCountIncidentsByEntityType() {
        // Given
        when(incidentRepository.countByEntityType(IncidentEntityType.TRIP))
                .thenReturn(5L);

        // When
        var count = service.countByEntityType(IncidentEntityType.TRIP);

        // Then
        assertThat(count).isEqualTo(5L);

        verify(incidentRepository).countByEntityType(IncidentEntityType.TRIP);
    }

    @Test
    void shouldListIncidentsByCreatedAtBetween() {
        // Given
        var start = OffsetDateTime.now().minusDays(7);
        var end = OffsetDateTime.now();

        var incident = Incident.builder()
                .id(100L)
                .entityType(IncidentEntityType.TRIP)
                .entityId(10L)
                .type(IncidentType.SECURITY)
                .note("Recent incident")
                .createdAt(OffsetDateTime.now().minusDays(3))
                .build();

        when(incidentRepository.findByCreatedAtBetween(start, end))
                .thenReturn(List.of(incident));

        // When
        var result = service.listByCreatedAtBetween(start, end);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(100L);

        verify(incidentRepository).findByCreatedAtBetween(start, end);
    }
}

