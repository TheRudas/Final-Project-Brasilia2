package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.ParcelServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.ParcelMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    @Mock
    private ParcelRepository repository;

    @Mock
    private StopRepository stopRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private ConfigService configService;

    @Spy
    private ParcelMapper mapper = Mappers.getMapper(ParcelMapper.class);

    @InjectMocks
    private ParcelServiceImpl service;

    @Test
    void shouldCreateParcelSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(1L).route(route).order(1).build();
        var toStop = Stop.builder().id(2L).route(route).order(3).build();

        var request = new ParcelCreateRequest(
                "PCL-001", "Juan Sender", "3001234567",
                "Maria Receiver", "3009876543", 1L, 2L
        );

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(configService.getValue("PARCEL_BASE_PRICE_PER_STOP")).thenReturn(new BigDecimal("2.50"));
        when(repository.existsByCode(anyString())).thenReturn(false);
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> {
            Parcel p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.senderName()).isEqualTo("Juan Sender");
        assertThat(response.receiverName()).isEqualTo("Maria Receiver");
        assertThat(response.fromStopId()).isEqualTo(1L);
        assertThat(response.toStopId()).isEqualTo(2L);

        verify(stopRepository).findById(1L);
        verify(stopRepository).findById(2L);
        verify(repository).save(any(Parcel.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenOriginStopNotExists() {
        // Given
        var request = new ParcelCreateRequest(
                "PCL-001", "Juan", "300", "Maria", "301", 99L, 2L
        );
        when(stopRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Origin stop not found");

        verify(stopRepository).findById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStopsAreFromDifferentRoutes() {
        // Given
        var route1 = Route.builder().id(1L).build();
        var route2 = Route.builder().id(2L).build();
        var fromStop = Stop.builder().id(1L).route(route1).order(1).build();
        var toStop = Stop.builder().id(2L).route(route2).order(3).build();

        var request = new ParcelCreateRequest(
                "PCL-001", "Juan", "300", "Maria", "301", 1L, 2L
        );

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must belong to the same route");

        verify(stopRepository).findById(1L);
        verify(stopRepository).findById(2L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenOriginIsAfterDestination() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(1L).route(route).order(5).build();
        var toStop = Stop.builder().id(2L).route(route).order(3).build();

        var request = new ParcelCreateRequest(
                "PCL-001", "Juan", "300", "Maria", "301", 1L, 2L
        );

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must come before destination stop");

        verify(stopRepository).findById(1L);
        verify(stopRepository).findById(2L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateParcelSuccessfully() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        var toStop = Stop.builder().id(2L).build();

        var existingParcel = Parcel.builder()
                .id(10L)
                .code("PCL-001")
                .senderName("Juan")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        var updateRequest = new ParcelUpdateRequest(
                "Juan Updated", "3111111111", null, null, null, null
        );

        when(repository.findById(10L)).thenReturn(Optional.of(existingParcel));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.senderName()).isEqualTo("Juan Updated");

        verify(repository).findById(10L);
        verify(repository).save(any(Parcel.class));
    }

    @Test
    void shouldGetParcelById() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        var toStop = Stop.builder().id(2L).build();

        var parcel = Parcel.builder()
                .id(10L)
                .code("PCL-001")
                .senderName("Juan Sender")
                .senderPhone("3001234567")
                .receiverName("Maria Receiver")
                .receiverPhone("3009876543")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("15000"))
                .status(ParcelStatus.CREATED)
                .deliveryOtp("12345678")
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(parcel));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo("PCL-001");
        assertThat(response.senderName()).isEqualTo("Juan Sender");

        verify(repository).findById(10L);
    }

    @Test
    void shouldDeleteParcel() {
        // Given
        var parcel = Parcel.builder().id(10L).build();
        when(repository.findById(10L)).thenReturn(Optional.of(parcel));

        // When
        service.delete(10L);

        // Then
        verify(repository).findById(10L);
        verify(repository).delete(parcel);
    }

    @Test
    void shouldGetParcelByCode() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        var toStop = Stop.builder().id(2L).build();

        var parcel = Parcel.builder()
                .id(10L)
                .code("PCL-20251120-00001")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        when(repository.findByCode("PCL-20251120-00001")).thenReturn(Optional.of(parcel));

        // When
        var response = service.getByCode("PCL-20251120-00001");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo("PCL-20251120-00001");

        verify(repository).findByCode("PCL-20251120-00001");
    }

    @Test
    void shouldGetParcelsByStatus() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        var toStop = Stop.builder().id(2L).build();

        var parcel1 = Parcel.builder()
                .id(1L).code("PCL-001")
                .fromStop(fromStop).toStop(toStop)
                .status(ParcelStatus.IN_TRANSIT).build();

        var parcel2 = Parcel.builder()
                .id(2L).code("PCL-002")
                .fromStop(fromStop).toStop(toStop)
                .status(ParcelStatus.IN_TRANSIT).build();

        when(repository.findByStatus(ParcelStatus.IN_TRANSIT)).thenReturn(List.of(parcel1, parcel2));

        // When
        var result = service.getByStatus(ParcelStatus.IN_TRANSIT);

        // Then
        assertThat(result).hasSize(2);

        verify(repository).findByStatus(ParcelStatus.IN_TRANSIT);
    }

    @Test
    void shouldDeliverParcelSuccessfully() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .code("PCL-001")
                .status(ParcelStatus.IN_TRANSIT)
                .deliveryOtp("12345678")
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(parcel));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.deliverParcel(10L, "12345678");

        // Then
        assertThat(response.id()).isEqualTo(10L);

        verify(repository).findById(10L);
        verify(repository).save(any(Parcel.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenOtpIsInvalid() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .code("PCL-001")
                .status(ParcelStatus.IN_TRANSIT)
                .deliveryOtp("12345678")
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(parcel));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When / Then
        assertThatThrownBy(() -> service.deliverParcel(10L, "99999999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid OTP");

        verify(repository).findById(10L);
        verify(repository).save(any(Parcel.class)); // Saved as FAILED
        verify(incidentRepository).save(any(Incident.class)); // Incident created
    }

    @Test
    void shouldAssignParcelToTrip() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .status(ParcelStatus.CREATED)
                .build();

        var trip = Trip.builder().id(5L).build();

        when(repository.findById(10L)).thenReturn(Optional.of(parcel));
        when(tripRepository.findById(5L)).thenReturn(Optional.of(trip));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.assignToTrip(10L, 5L);

        // Then
        assertThat(response.id()).isEqualTo(10L);

        verify(repository).findById(10L);
        verify(tripRepository).findById(5L);
        verify(repository).save(any(Parcel.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAssigningNonCreatedParcel() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .status(ParcelStatus.IN_TRANSIT)
                .build();

        var trip = Trip.builder().id(5L).build();

        when(repository.findById(10L)).thenReturn(Optional.of(parcel));
        when(tripRepository.findById(5L)).thenReturn(Optional.of(trip));

        // When / Then
        assertThatThrownBy(() -> service.assignToTrip(10L, 5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only CREATED parcels can be assigned");

        verify(repository).findById(10L);
        verify(tripRepository).findById(5L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateParcelStatus() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .status(ParcelStatus.CREATED)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(parcel));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.updateStatus(10L, ParcelStatus.IN_TRANSIT);

        // Then
        assertThat(response.id()).isEqualTo(10L);

        verify(repository).findById(10L);
        verify(repository).save(any(Parcel.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenInvalidStatusTransition() {
        // Given
        var parcel = Parcel.builder()
                .id(10L)
                .status(ParcelStatus.DELIVERED)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(parcel));

        // When / Then
        assertThatThrownBy(() -> service.updateStatus(10L, ParcelStatus.IN_TRANSIT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DELIVERED is a final state");

        verify(repository).findById(10L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldListParcelsForDelivery() {
        // Given
        var fromStop = Stop.builder().id(1L).build();
        var toStop = Stop.builder().id(2L).build();

        var parcel1 = Parcel.builder()
                .id(1L).code("PCL-001")
                .fromStop(fromStop).toStop(toStop)
                .status(ParcelStatus.IN_TRANSIT).build();

        when(repository.findByToStopIdAndStatus(2L, ParcelStatus.IN_TRANSIT))
                .thenReturn(List.of(parcel1));

        // When
        var result = service.listParcelsForDelivery(2L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).toStopId()).isEqualTo(2L);

        verify(repository).findByToStopIdAndStatus(2L, ParcelStatus.IN_TRANSIT);
    }
}

