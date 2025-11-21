package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.BusServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.BusMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusServiceImplTest {

    @Mock
    private BusRepository repository;

    @Mock
    private TripRepository tripRepository;

    @Spy
    private BusMapper mapper = Mappers.getMapper(BusMapper.class);

    @InjectMocks
    private BusServiceImpl service;

    @Test
    void shouldCreateBusSuccessfully() {
        // Given
        var request = new BusCreateRequest("ABC-123", 40, true);

        when(repository.findByPlate("ABC-123")).thenReturn(Optional.empty());
        when(repository.save(any(Bus.class))).thenAnswer(inv -> {
            Bus b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.licensePlate()).isEqualTo("ABC-123");
        assertThat(response.capacity()).isEqualTo(40);
        assertThat(response.status()).isTrue();

        verify(repository).findByPlate("ABC-123");
        verify(repository).save(any(Bus.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenPlateAlreadyExists() {
        // Given
        var request = new BusCreateRequest("ABC-123", 40, true);
        var existingBus = Bus.builder().id(1L).plate("ABC-123").build();

        when(repository.findByPlate("ABC-123")).thenReturn(Optional.of(existingBus));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("A bus with plate ABC-123 already exists");

        verify(repository).findByPlate("ABC-123");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateBusSuccessfully() {
        // Given
        var existingBus = Bus.builder()
                .id(10L)
                .plate("ABC-123")
                .capacity(40)
                .status(true)
                .build();

        var updateRequest = new BusUpdateRequest("XYZ-789", 50, false);

        when(repository.findById(10L)).thenReturn(Optional.of(existingBus));
        when(repository.save(any(Bus.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.licensePlate()).isEqualTo("XYZ-789");
        assertThat(response.capacity()).isEqualTo(50);
        assertThat(response.status()).isFalse();

        verify(repository).findById(10L);
        verify(repository).save(any(Bus.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentBus() {
        // Given
        var updateRequest = new BusUpdateRequest("XYZ-789", 50, false);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus with Id 99 not found");

        verify(repository).findById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldGetBusById() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC-123")
                .capacity(40)
                .status(true)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(bus));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.licensePlate()).isEqualTo("ABC-123");
        assertThat(response.capacity()).isEqualTo(40);

        verify(repository).findById(10L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBusNotExists() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus 99 not found");

        verify(repository).findById(99L);
    }

    @Test
    void shouldDeleteBus() {
        // Given
        var bus = Bus.builder().id(10L).plate("ABC-123").build();
        when(repository.findById(10L)).thenReturn(Optional.of(bus));
        when(tripRepository.existsByBusId(10L)).thenReturn(false);

        // When
        service.delete(10L);

        // Then
        verify(repository).findById(10L);
        verify(tripRepository).existsByBusId(10L);
        verify(repository).delete(bus);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenDeletingBusWithTrips() {
        // Given
        var bus = Bus.builder().id(10L).plate("ABC-123").build();
        when(repository.findById(10L)).thenReturn(Optional.of(bus));
        when(tripRepository.existsByBusId(10L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete bus 10")
                .hasMessageContaining("it has trips assigned");

        verify(repository).findById(10L);
        verify(tripRepository).existsByBusId(10L);
        verify(repository, never()).delete(any());
    }

    @Test
    void shouldGetBusByLicensePlate() {
        // Given
        var bus = Bus.builder()
                .id(10L)
                .plate("ABC-123")
                .capacity(40)
                .status(true)
                .build();

        when(repository.findByPlate("ABC-123")).thenReturn(Optional.of(bus));

        // When
        var response = service.getByLicensePlate("ABC-123");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.licensePlate()).isEqualTo("ABC-123");

        verify(repository).findByPlate("ABC-123");
    }

    @Test
    void shouldGetBusesByCapacityGreaterThanEqual() {
        // Given
        var bus1 = Bus.builder().id(1L).plate("ABC-123").capacity(50).status(true).build();
        var bus2 = Bus.builder().id(2L).plate("XYZ-789").capacity(60).status(true).build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(bus1, bus2));

        when(repository.findByCapacityGreaterThanEqual(50, pageable)).thenReturn(page);

        // When
        var result = service.getByCapacityGreaterThanEqual(50, 0, 10);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).capacity()).isGreaterThanOrEqualTo(50);

        verify(repository).findByCapacityGreaterThanEqual(50, pageable);
    }

    @Test
    void shouldGetBusesByCapacityLessThanEqual() {
        // Given
        var bus1 = Bus.builder().id(1L).plate("ABC-123").capacity(30).status(true).build();
        var bus2 = Bus.builder().id(2L).plate("XYZ-789").capacity(40).status(true).build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(bus1, bus2));

        when(repository.findByCapacityLessThanEqual(40, pageable)).thenReturn(page);

        // When
        var result = service.getByCapacityLessThanEqual(40, 0, 10);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).capacity()).isLessThanOrEqualTo(40);

        verify(repository).findByCapacityLessThanEqual(40, pageable);
    }

    @Test
    void shouldGetBusesByCapacityBetween() {
        // Given
        var bus1 = Bus.builder().id(1L).plate("ABC-123").capacity(35).status(true).build();
        var bus2 = Bus.builder().id(2L).plate("XYZ-789").capacity(45).status(true).build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(bus1, bus2));

        when(repository.findByCapacityBetween(30, 50, pageable)).thenReturn(page);

        // When
        var result = service.getByCapacityBetween(30, 50, 0, 10);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).capacity()).isBetween(30, 50);

        verify(repository).findByCapacityBetween(30, 50, pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoCapacityMatchFound() {
        // Given
        var pageable = PageRequest.of(0, 10);
        var emptyPage = new PageImpl<Bus>(List.of());

        when(repository.findByCapacityGreaterThanEqual(100, pageable)).thenReturn(emptyPage);

        // When / Then
        assertThatThrownBy(() -> service.getByCapacityGreaterThanEqual(100, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No buses found with capacity >= 100");

        verify(repository).findByCapacityGreaterThanEqual(100, pageable);
    }
}

