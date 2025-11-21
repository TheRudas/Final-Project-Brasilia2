package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.SeatCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.SeatUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Seat;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.SeatRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.SeatServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.SeatMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private BusRepository busRepository;

    @Spy
    private SeatMapper mapper = Mappers.getMapper(SeatMapper.class);

    @InjectMocks
    private SeatServiceImpl service;

    @Test
    void shouldCreateSeatSuccessfully() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .build();

        var request = new SeatCreateRequest(
                1L,
                "A1",
                SeatType.STANDARD
        );

        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.existsByBusIdAndNumber(1L, "A1")).thenReturn(false);
        when(seatRepository.countByBusId(1L)).thenReturn(10L);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> {
            Seat s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.busId()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("A1");
        assertThat(response.seatType()).isEqualTo(SeatType.STANDARD);

        verify(busRepository).findById(1L);
        verify(seatRepository).existsByBusIdAndNumber(1L, "A1");
        verify(seatRepository).countByBusId(1L);
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBusNotExists() {
        // Given
        var request = new SeatCreateRequest(
                99L,
                "A1",
                SeatType.STANDARD
        );

        when(busRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("bus 99 not found");

        verify(busRepository).findById(99L);
        verify(seatRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSeatNumberAlreadyExists() {
        // Given
        var bus = Bus.builder().id(1L).capacity(40).build();
        var request = new SeatCreateRequest(
                1L,
                "A1",
                SeatType.STANDARD
        );

        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.existsByBusIdAndNumber(1L, "A1")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seat A1 already exists in this bus");

        verify(seatRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenBusCapacityExceeded() {
        // Given
        var bus = Bus.builder().id(1L).capacity(40).build();
        var request = new SeatCreateRequest(
                1L,
                "A1",
                SeatType.STANDARD
        );

        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.existsByBusIdAndNumber(1L, "A1")).thenReturn(false);
        when(seatRepository.countByBusId(1L)).thenReturn(40L);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Bus capacity exceeded: 40/40 seats already registered");

        verify(seatRepository, never()).save(any());
    }

    @Test
    void shouldUpdateSeatSuccessfully() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var existingSeat = Seat.builder()
                .id(100L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                "A2",
                SeatType.PREFERENTIAL
        );

        when(seatRepository.findById(100L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.existsByBusIdAndNumber(1L, "A2")).thenReturn(false);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(100L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.number()).isEqualTo("A2");
        assertThat(response.seatType()).isEqualTo(SeatType.PREFERENTIAL);

        verify(seatRepository).findById(100L);
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentSeat() {
        // Given
        var updateRequest = new SeatUpdateRequest(
                "A2",
                SeatType.STANDARD
        );

        when(seatRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("seat 99 not found");

        verify(seatRepository).findById(99L);
        verify(seatRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUpdateToExistingSeatNumber() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var existingSeat = Seat.builder()
                .id(100L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                "A2",
                SeatType.STANDARD
        );

        when(seatRepository.findById(100L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.existsByBusIdAndNumber(1L, "A2")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.update(100L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seat A2 already exists in this bus");

        verify(seatRepository, never()).save(any());
    }

    @Test
    void shouldGetSeatById() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(100L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        when(seatRepository.findById(100L)).thenReturn(Optional.of(seat));

        // When
        var response = service.get(100L);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.number()).isEqualTo("A1");

        verify(seatRepository).findById(100L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentSeat() {
        // Given
        when(seatRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("seat 99 not found");

        verify(seatRepository).findById(99L);
    }

    @Test
    void shouldDeleteSeatSuccessfully() {
        // Given
        var seat = Seat.builder()
                .id(100L)
                .number("A1")
                .build();

        when(seatRepository.findById(100L)).thenReturn(Optional.of(seat));
        doNothing().when(seatRepository).delete(seat);

        // When
        service.delete(100L);

        // Then
        verify(seatRepository).findById(100L);
        verify(seatRepository).delete(seat);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentSeat() {
        // Given
        when(seatRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("seat 99 not found");

        verify(seatRepository).findById(99L);
        verify(seatRepository, never()).delete(any());
    }

    @Test
    void shouldListSeatsByBusId() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat1 = Seat.builder().id(100L).bus(bus).number("A1").seatType(SeatType.STANDARD).build();
        var seat2 = Seat.builder().id(101L).bus(bus).number("A2").seatType(SeatType.STANDARD).build();

        when(seatRepository.findByBusId(1L))
                .thenReturn(List.of(seat1, seat2));

        // When
        var result = service.listByBusId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).busId()).isEqualTo(1L);
        assertThat(result.get(1).busId()).isEqualTo(1L);

        verify(seatRepository).findByBusId(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBusHasNoSeats() {
        // Given
        when(seatRepository.findByBusId(99L))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByBusId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("bus 99 has no seats");

        verify(seatRepository).findByBusId(99L);
    }

    @Test
    void shouldListSeatsByBusIdAndSeatType() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(100L)
                .bus(bus)
                .number("P1")
                .seatType(SeatType.PREFERENTIAL)
                .build();

        when(seatRepository.findByBusIdAndSeatType(1L, SeatType.PREFERENTIAL))
                .thenReturn(List.of(seat));

        // When
        var result = service.listByBusIdAndSeatType(1L, SeatType.PREFERENTIAL);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).seatType()).isEqualTo(SeatType.PREFERENTIAL);

        verify(seatRepository).findByBusIdAndSeatType(1L, SeatType.PREFERENTIAL);
    }

    @Test
    void shouldGetSeatByBusIdAndNumber() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat = Seat.builder()
                .id(100L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        when(seatRepository.findByBusIdAndNumber(1L, "A1"))
                .thenReturn(Optional.of(seat));

        // When
        var response = service.getByBusIdAndNumber(1L, "A1");

        // Then
        assertThat(response.number()).isEqualTo("A1");
        assertThat(response.busId()).isEqualTo(1L);

        verify(seatRepository).findByBusIdAndNumber(1L, "A1");
    }

    @Test
    void shouldListSeatsByBusIdOrderedByNumber() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var seat1 = Seat.builder().id(100L).bus(bus).number("A1").build();
        var seat2 = Seat.builder().id(101L).bus(bus).number("A2").build();

        when(seatRepository.findByBusIdOrderByNumberAsc(1L))
                .thenReturn(List.of(seat1, seat2));

        // When
        var result = service.listByBusIdOrderByNumberAsc(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).number()).isEqualTo("A1");
        assertThat(result.get(1).number()).isEqualTo("A2");

        verify(seatRepository).findByBusIdOrderByNumberAsc(1L);
    }

    @Test
    void shouldCountSeatsByBusId() {
        // Given
        when(seatRepository.countByBusId(1L)).thenReturn(40L);

        // When
        var count = service.countByBusId(1L);

        // Then
        assertThat(count).isEqualTo(40L);

        verify(seatRepository).countByBusId(1L);
    }
}

