package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.SeatHoldCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.SeatHoldServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.SeatHoldMapper;
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
class SeatHoldServiceImplTest {

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SeatRepository seatRepository;

    @Spy
    private SeatHoldMapper mapper = Mappers.getMapper(SeatHoldMapper.class);

    @InjectMocks
    private SeatHoldServiceImpl service;

    @Test
    void shouldCreateSeatHoldSuccessfully() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(10L).bus(bus).build();
        var user = User.builder().id(5L).name("Juan Perez").build();
        var seat = Seat.builder().id(1L).bus(bus).number("A1").build();

        var request = new SeatHoldCreateRequest(
                10L,
                "A1",
                5L
        );

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(seatRepository.findByBusIdAndNumber(1L, "A1")).thenReturn(Optional.of(seat));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A1")).thenReturn(Optional.empty());
        when(seatHoldRepository.findByTripId(10L)).thenReturn(List.of());
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(inv -> {
            SeatHold sh = inv.getArgument(0);
            sh.setId(100L);
            return sh;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.tripId()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A1");
        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.status()).isEqualTo(SeatHoldStatus.HOLD);

        verify(tripRepository).findById(10L);
        verify(userRepository).findById(5L);
        verify(seatRepository).findByBusIdAndNumber(1L, "A1");
        verify(ticketRepository).findByTripAndSeatNumber(trip, "A1");
        verify(seatHoldRepository).save(any(SeatHold.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExists() {
        // Given
        var request = new SeatHoldCreateRequest(
                99L,
                "A1",
                5L
        );

        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(tripRepository).findById(99L);
        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotExists() {
        // Given
        var bus = Bus.builder().id(1L).build();
        var trip = Trip.builder().id(10L).bus(bus).build();

        var request = new SeatHoldCreateRequest(
                10L,
                "A1",
                99L
        );

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User 99 not found");

        verify(userRepository).findById(99L);
        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenSeatNotExistsInBus() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(10L).bus(bus).build();
        var user = User.builder().id(5L).build();

        var request = new SeatHoldCreateRequest(
                10L,
                "Z99",
                5L
        );

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(seatRepository.findByBusIdAndNumber(1L, "Z99")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Seat Z99 not found in bus ABC123");

        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSeatAlreadySold() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(10L).bus(bus).build();
        var user = User.builder().id(5L).build();
        var seat = Seat.builder().id(1L).bus(bus).number("A1").build();
        var ticket = Ticket.builder().id(1L).seatNumber("A1").build();

        var request = new SeatHoldCreateRequest(
                10L,
                "A1",
                5L
        );

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(seatRepository.findByBusIdAndNumber(1L, "A1")).thenReturn(Optional.of(seat));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A1")).thenReturn(Optional.of(ticket));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat A1 already sold for this trip");

        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSeatIsCurrentlyHeld() {
        // Given
        var bus = Bus.builder().id(1L).plate("ABC123").build();
        var trip = Trip.builder().id(10L).bus(bus).build();
        var user = User.builder().id(5L).build();
        var seat = Seat.builder().id(1L).bus(bus).number("A1").build();

        var existingHold = SeatHold.builder()
                .id(50L)
                .trip(trip)
                .seatNumber("A1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .build();

        var request = new SeatHoldCreateRequest(
                10L,
                "A1",
                5L
        );

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(seatRepository.findByBusIdAndNumber(1L, "A1")).thenReturn(Optional.of(seat));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A1")).thenReturn(Optional.empty());
        when(seatHoldRepository.findByTripId(10L)).thenReturn(List.of(existingHold));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat A1 is currently held");

        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldGetSeatHoldById() {
        // Given
        var trip = Trip.builder().id(10L).build();
        var user = User.builder().id(5L).build();

        var seatHold = SeatHold.builder()
                .id(100L)
                .trip(trip)
                .seatNumber("A1")
                .user(user)
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        when(seatHoldRepository.findById(100L)).thenReturn(Optional.of(seatHold));

        // When
        var response = service.get(100L);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.seatNumber()).isEqualTo("A1");

        verify(seatHoldRepository).findById(100L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentSeatHold() {
        // Given
        when(seatHoldRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("SeatHold 99 not found");

        verify(seatHoldRepository).findById(99L);
    }

    @Test
    void shouldListSeatHoldsByTripId() {
        // Given
        var trip = Trip.builder().id(10L).build();
        var user = User.builder().id(5L).build();

        var hold1 = SeatHold.builder().id(100L).trip(trip).user(user).seatNumber("A1").build();
        var hold2 = SeatHold.builder().id(101L).trip(trip).user(user).seatNumber("A2").build();

        when(seatHoldRepository.findByTripId(10L))
                .thenReturn(List.of(hold1, hold2));

        // When
        var result = service.listByTripId(10L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).tripId()).isEqualTo(10L);
        assertThat(result.get(1).tripId()).isEqualTo(10L);

        verify(seatHoldRepository).findByTripId(10L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoHoldsForTrip() {
        // Given
        when(seatHoldRepository.findByTripId(99L))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByTripId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No holds found for trip 99");

        verify(seatHoldRepository).findByTripId(99L);
    }

    @Test
    void shouldListSeatHoldsByUserId() {
        // Given
        var trip = Trip.builder().id(10L).build();
        var user = User.builder().id(5L).build();

        var hold = SeatHold.builder().id(100L).trip(trip).user(user).seatNumber("A1").build();

        when(seatHoldRepository.findByUserId(5L))
                .thenReturn(List.of(hold));

        // When
        var result = service.listByUserId(5L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(5L);

        verify(seatHoldRepository).findByUserId(5L);
    }

    @Test
    void shouldExpireSeatHold() {
        // Given
        var seatHold = SeatHold.builder()
                .id(100L)
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();

        when(seatHoldRepository.findById(100L)).thenReturn(Optional.of(seatHold));
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.expire(100L);

        // Then
        verify(seatHoldRepository).findById(100L);
        verify(seatHoldRepository).save(any(SeatHold.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenExpireNonHoldStatus() {
        // Given
        var seatHold = SeatHold.builder()
                .id(100L)
                .status(SeatHoldStatus.EXPIRED)
                .build();

        when(seatHoldRepository.findById(100L)).thenReturn(Optional.of(seatHold));

        // When / Then
        assertThatThrownBy(() -> service.expire(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only HOLD status can be expired");

        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldExpireAllExpiredHolds() {
        // Given
        var now = OffsetDateTime.now();
        var hold1 = SeatHold.builder()
                .id(100L)
                .status(SeatHoldStatus.HOLD)
                .expiresAt(now.minusMinutes(5))
                .build();

        var hold2 = SeatHold.builder()
                .id(101L)
                .status(SeatHoldStatus.HOLD)
                .expiresAt(now.minusMinutes(2))
                .build();

        when(seatHoldRepository.findByStatusAndExpiresAtBefore(eq(SeatHoldStatus.HOLD), any(OffsetDateTime.class)))
                .thenReturn(List.of(hold1, hold2));
        when(seatHoldRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.expireAll();

        // Then
        verify(seatHoldRepository).findByStatusAndExpiresAtBefore(eq(SeatHoldStatus.HOLD), any(OffsetDateTime.class));
        verify(seatHoldRepository).saveAll(anyList());
    }
}

