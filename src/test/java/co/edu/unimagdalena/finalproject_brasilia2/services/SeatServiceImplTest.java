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

import java.util.HashSet;
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

    // ============= CREATE TESTS =============

    @Test
    void shouldCreateSeatSuccessfully() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var request = new SeatCreateRequest(
                1L,
                "A1",
                SeatType.STANDARD
        );

        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.existsByBusIdAndNumber(1L, "A1")).thenReturn(false);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> {
            Seat s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.busId()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("A1");
        assertThat(response.seatType()).isEqualTo(SeatType.STANDARD);

        verify(busRepository).findById(1L);
        verify(seatRepository).existsByBusIdAndNumber(1L, "A1");
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void shouldCreatePreferentialSeat() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var request = new SeatCreateRequest(
                1L,
                "P1",
                SeatType.PREFERENTIAL
        );

        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.existsByBusIdAndNumber(1L, "P1")).thenReturn(false);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> {
            Seat s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.seatType()).isEqualTo(SeatType.PREFERENTIAL);

        verify(busRepository).findById(1L);
        verify(seatRepository).existsByBusIdAndNumber(1L, "P1");
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
        verify(seatRepository, never()).existsByBusIdAndNumber(any(), any());
        verify(seatRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSeatNumberAlreadyExistsInBus() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

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

        verify(busRepository).findById(1L);
        verify(seatRepository).existsByBusIdAndNumber(1L, "A1");
        verify(seatRepository, never()).save(any());
    }

    // ============= UPDATE TESTS =============

    @Test
    void shouldUpdateSeatSuccessfully() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var existingSeat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                "A2",
                SeatType.PREFERENTIAL
        );

        when(seatRepository.findById(10L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.existsByBusIdAndNumber(1L, "A2")).thenReturn(false);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.number()).isEqualTo("A2");
        assertThat(response.seatType()).isEqualTo(SeatType.PREFERENTIAL);

        verify(seatRepository).findById(10L);
        verify(seatRepository).existsByBusIdAndNumber(1L, "A2");
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void shouldUpdateOnlySeatType() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var existingSeat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                null,
                SeatType.PREFERENTIAL
        );

        when(seatRepository.findById(10L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.number()).isEqualTo("A1"); // No cambió
        assertThat(response.seatType()).isEqualTo(SeatType.PREFERENTIAL);

        verify(seatRepository).findById(10L);
        verify(seatRepository, never()).existsByBusIdAndNumber(any(), any());
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void shouldUpdateSeatWithSameNumber() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var existingSeat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                "A1", // Mismo número
                SeatType.PREFERENTIAL
        );

        when(seatRepository.findById(10L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.number()).isEqualTo("A1");
        assertThat(response.seatType()).isEqualTo(SeatType.PREFERENTIAL);

        verify(seatRepository).findById(10L);
        verify(seatRepository, never()).existsByBusIdAndNumber(any(), any()); // No valida porque es el mismo número
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUpdatingToExistingSeatNumber() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var existingSeat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var updateRequest = new SeatUpdateRequest(
                "A2", // Ya existe en el bus
                SeatType.PREFERENTIAL
        );

        when(seatRepository.findById(10L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.existsByBusIdAndNumber(1L, "A2")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.update(10L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seat A2 already exists in this bus");

        verify(seatRepository).findById(10L);
        verify(seatRepository).existsByBusIdAndNumber(1L, "A2");
        verify(seatRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentSeat() {
        // Given
        var updateRequest = new SeatUpdateRequest(
                "A2",
                SeatType.PREFERENTIAL
        );
        when(seatRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("seat 99 not found");

        verify(seatRepository).findById(99L);
        verify(seatRepository, never()).save(any());
    }

    // ============= GET BY ID TESTS =============

    @Test
    void shouldGetSeatById() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.busId()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("A1");
        assertThat(response.seatType()).isEqualTo(SeatType.STANDARD);

        verify(seatRepository).findById(10L);
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

    // ============= DELETE TESTS =============

    @Test
    void shouldDeleteSeatSuccessfully() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        doNothing().when(seatRepository).delete(seat);

        // When
        service.delete(10L);

        // Then
        verify(seatRepository).findById(10L);
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

    // ============= GET BY BUS ID TESTS =============

    @Test
    void shouldGetSeatsByBusId() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var seat1 = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var seat2 = Seat.builder()
                .id(11L)
                .bus(bus)
                .number("A2")
                .seatType(SeatType.STANDARD)
                .build();

        when(seatRepository.findByBusId(1L)).thenReturn(List.of(seat1, seat2));

        // When
        var result = service.getByBusId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).busId()).isEqualTo(1L);
        assertThat(result.get(0).number()).isEqualTo("A1");
        assertThat(result.get(1).busId()).isEqualTo(1L);
        assertThat(result.get(1).number()).isEqualTo("A2");

        verify(seatRepository).findByBusId(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBusHasNoSeats() {
        // Given
        when(seatRepository.findByBusId(1L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByBusId(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("bus 1 has no seats");

        verify(seatRepository).findByBusId(1L);
    }

    // ============= GET BY BUS ID AND SEAT TYPE TESTS =============

    @Test
    void shouldGetStandardSeatsByBusId() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var seat1 = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var seat2 = Seat.builder()
                .id(11L)
                .bus(bus)
                .number("A2")
                .seatType(SeatType.STANDARD)
                .build();

        when(seatRepository.findByBusIdAndSeatType(1L, SeatType.STANDARD))
                .thenReturn(List.of(seat1, seat2));

        // When
        var result = service.getByBusIdAndSeatType(1L, SeatType.STANDARD);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).seatType()).isEqualTo(SeatType.STANDARD);
        assertThat(result.get(1).seatType()).isEqualTo(SeatType.STANDARD);

        verify(seatRepository).findByBusIdAndSeatType(1L, SeatType.STANDARD);
    }

    @Test
    void shouldGetPreferentialSeatsByBusId() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var seat1 = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("P1")
                .seatType(SeatType.PREFERENTIAL)
                .build();

        when(seatRepository.findByBusIdAndSeatType(1L, SeatType.PREFERENTIAL))
                .thenReturn(List.of(seat1));

        // When
        var result = service.getByBusIdAndSeatType(1L, SeatType.PREFERENTIAL);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).seatType()).isEqualTo(SeatType.PREFERENTIAL);

        verify(seatRepository).findByBusIdAndSeatType(1L, SeatType.PREFERENTIAL);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBusHasNoSeatsOfType() {
        // Given
        when(seatRepository.findByBusIdAndSeatType(1L, SeatType.PREFERENTIAL))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByBusIdAndSeatType(1L, SeatType.PREFERENTIAL))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("bus 1 has no PREFERENTIAL seats");

        verify(seatRepository).findByBusIdAndSeatType(1L, SeatType.PREFERENTIAL);
    }

    // ============= GET BY BUS ID AND NUMBER TESTS =============

    @Test
    void shouldGetSeatByBusIdAndNumber() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var seat = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        when(seatRepository.findByBusIdAndNumber(1L, "A1"))
                .thenReturn(Optional.of(seat));

        // When
        var response = service.getByBusIdAndNumber(1L, "A1");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.busId()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo("A1");

        verify(seatRepository).findByBusIdAndNumber(1L, "A1");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenSeatNumberNotFoundInBus() {
        // Given
        when(seatRepository.findByBusIdAndNumber(1L, "Z99"))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByBusIdAndNumber(1L, "Z99"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("seat number Z99 not found in this bus");

        verify(seatRepository).findByBusIdAndNumber(1L, "Z99");
    }

    // ============= GET BY BUS ID ORDER BY NUMBER ASC TESTS =============

    @Test
    void shouldGetSeatsByBusIdOrderedByNumber() {
        // Given
        var bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();

        var seat1 = Seat.builder()
                .id(10L)
                .bus(bus)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        var seat2 = Seat.builder()
                .id(11L)
                .bus(bus)
                .number("A2")
                .seatType(SeatType.STANDARD)
                .build();

        var seat3 = Seat.builder()
                .id(12L)
                .bus(bus)
                .number("A3")
                .seatType(SeatType.PREFERENTIAL)
                .build();

        when(seatRepository.findByBusIdOrderByNumberAsc(1L))
                .thenReturn(List.of(seat1, seat2, seat3));

        // When
        var result = service.getByBusIdOrderByNumberAsc(1L);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).number()).isEqualTo("A1");
        assertThat(result.get(1).number()).isEqualTo("A2");
        assertThat(result.get(2).number()).isEqualTo("A3");

        verify(seatRepository).findByBusIdOrderByNumberAsc(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBusHasNoSeatsForOrdering() {
        // Given
        when(seatRepository.findByBusIdOrderByNumberAsc(1L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByBusIdOrderByNumberAsc(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("bus 1 has no seats");

        verify(seatRepository).findByBusIdOrderByNumberAsc(1L);
    }

    // ============= COUNT BY BUS ID TESTS =============

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

    @Test
    void shouldReturnZeroWhenBusHasNoSeats() {
        // Given
        when(seatRepository.countByBusId(1L)).thenReturn(0L);

        // When
        var count = service.countByBusId(1L);

        // Then
        assertThat(count).isEqualTo(0L);

        verify(seatRepository).countByBusId(1L);
    }

    @Test
    void shouldCountOnlyStandardSeatsInBus() {
        // Given
        when(seatRepository.countByBusId(1L)).thenReturn(35L);

        // When
        var count = service.countByBusId(1L);

        // Then
        assertThat(count).isEqualTo(35L);

        verify(seatRepository).countByBusId(1L);
    }
}