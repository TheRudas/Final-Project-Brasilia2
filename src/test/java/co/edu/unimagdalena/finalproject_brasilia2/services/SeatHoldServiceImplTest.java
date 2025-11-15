package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
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

    @Spy
    private SeatHoldMapper mapper = Mappers.getMapper(SeatHoldMapper.class);

    @InjectMocks
    private SeatHoldServiceImpl service;

    // Helper methods
    private Route createTestRoute() {
        return Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Bogotá-Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .distanceKm(new BigDecimal("400.00"))
                .durationMin(360)
                .build();
    }

    private Bus createTestBus() {
        return Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();
    }

    private Trip createTestTrip(Route route, Bus bus) {
        return Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();
    }

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .name("Juan Pérez")
                .email("juan@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hash")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    // ============= CREATE TESTS =============

    @Test
    void shouldCreateSeatHoldSuccessfully() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var user = createTestUser();

        var request = new SeatHoldCreateRequest(1L, "A1", 1L);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A1")).thenReturn(Optional.empty());
        when(seatHoldRepository.findByTripId(1L)).thenReturn(List.of());
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(inv -> {
            SeatHold sh = inv.getArgument(0);
            sh.setId(10L);
            return sh;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.seatNumber()).isEqualTo("A1");
        assertThat(response.status()).isEqualTo(SeatHoldStatus.HOLD);
        assertThat(response.expiresAt()).isAfter(OffsetDateTime.now());

        verify(tripRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(ticketRepository).findByTripAndSeatNumber(trip, "A1");
        verify(seatHoldRepository).findByTripId(1L);
        verify(seatHoldRepository).save(any(SeatHold.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTripNotExists() {
        // Given
        var request = new SeatHoldCreateRequest(99L, "A1", 1L);
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 99 not found");

        verify(tripRepository).findById(99L);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotExists() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);

        var request = new SeatHoldCreateRequest(1L, "A1", 99L);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User 99 not found");

        verify(tripRepository).findById(1L);
        verify(userRepository).findById(99L);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSeatAlreadySold() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var user = createTestUser();

        var existingTicket = Ticket.builder()
                .id(1L)
                .trip(trip)
                .seatNumber("A1")
                .build();

        var request = new SeatHoldCreateRequest(1L, "A1", 1L);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A1"))
                .thenReturn(Optional.of(existingTicket));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat A1 already sold for this trip");

        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSeatCurrentlyHeld() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var user = createTestUser();

        var existingHold = SeatHold.builder()
                .id(5L)
                .trip(trip)
                .user(user)
                .seatNumber("A1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .build();

        var request = new SeatHoldCreateRequest(1L, "A1", 1L);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A1")).thenReturn(Optional.empty());
        when(seatHoldRepository.findByTripId(1L)).thenReturn(List.of(existingHold));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat A1 is currently held");

        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldCreateSeatHoldWhenExistingHoldIsExpired() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var user = createTestUser();

        var expiredHold = SeatHold.builder()
                .id(5L)
                .trip(trip)
                .user(user)
                .seatNumber("A1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().minusMinutes(5)) // Expired
                .build();

        var request = new SeatHoldCreateRequest(1L, "A1", 1L);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.findByTripAndSeatNumber(trip, "A1")).thenReturn(Optional.empty());
        when(seatHoldRepository.findByTripId(1L)).thenReturn(List.of(expiredHold));
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(inv -> {
            SeatHold sh = inv.getArgument(0);
            sh.setId(10L);
            return sh;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A1");
        assertThat(response.status()).isEqualTo(SeatHoldStatus.HOLD);

        verify(seatHoldRepository).save(any(SeatHold.class));
    }

    // ============= GET BY ID TESTS =============

    @Test
    void shouldGetSeatHoldById() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var user = User.builder().id(1L).name("Juan").build();

        var seatHold = SeatHold.builder()
                .id(10L)
                .trip(trip)
                .user(user)
                .seatNumber("A1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        when(seatHoldRepository.findById(10L)).thenReturn(Optional.of(seatHold));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.seatNumber()).isEqualTo("A1");
        assertThat(response.status()).isEqualTo(SeatHoldStatus.HOLD);

        verify(seatHoldRepository).findById(10L);
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

    // ============= GET BY TRIP ID TESTS =============

    @Test
    void shouldGetSeatHoldsByTripId() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var user = User.builder().id(1L).name("Juan").build();

        var hold1 = SeatHold.builder()
                .id(10L).trip(trip).user(user).seatNumber("A1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        var hold2 = SeatHold.builder()
                .id(11L).trip(trip).user(user).seatNumber("A2")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        when(seatHoldRepository.findByTripId(1L)).thenReturn(List.of(hold1, hold2));

        // When
        var result = service.getByTripId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).tripId()).isEqualTo(1L);
        assertThat(result.get(1).tripId()).isEqualTo(1L);

        verify(seatHoldRepository).findByTripId(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoHoldsForTrip() {
        // Given
        when(seatHoldRepository.findByTripId(99L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByTripId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No holds found for trip 99");

        verify(seatHoldRepository).findByTripId(99L);
    }

    // ============= GET BY USER ID TESTS =============

    @Test
    void shouldGetSeatHoldsByUserId() {
        // Given
        var trip = Trip.builder().id(1L).build();
        var user = User.builder().id(1L).name("Juan").build();

        var hold1 = SeatHold.builder()
                .id(10L).trip(trip).user(user).seatNumber("A1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        var hold2 = SeatHold.builder()
                .id(11L).trip(trip).user(user).seatNumber("B1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        when(seatHoldRepository.findByUserId(1L)).thenReturn(List.of(hold1, hold2));

        // When
        var result = service.getByUserId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).userId()).isEqualTo(1L);
        assertThat(result.get(1).userId()).isEqualTo(1L);

        verify(seatHoldRepository).findByUserId(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoHoldsForUser() {
        // Given
        when(seatHoldRepository.findByUserId(99L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByUserId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No holds found for user 99");

        verify(seatHoldRepository).findByUserId(99L);
    }

    // ============= EXPIRE TESTS =============

    @Test
    void shouldExpireSeatHoldSuccessfully() {
        // Given
        var seatHold = SeatHold.builder()
                .id(10L)
                .seatNumber("A1")
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();

        when(seatHoldRepository.findById(10L)).thenReturn(Optional.of(seatHold));
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.expire(10L);

        // Then
        verify(seatHoldRepository).findById(10L);
        verify(seatHoldRepository).save(argThat(hold ->
                hold.getStatus() == SeatHoldStatus.EXPIRED
        ));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenExpireNonExistentHold() {
        // Given
        when(seatHoldRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.expire(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("SeatHold 99 not found");

        verify(seatHoldRepository).findById(99L);
        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenExpireNonHoldStatus() {
        // Given
        var seatHold = SeatHold.builder()
                .id(10L)
                .status(SeatHoldStatus.EXPIRED)
                .build();

        when(seatHoldRepository.findById(10L)).thenReturn(Optional.of(seatHold));

        // When / Then
        assertThatThrownBy(() -> service.expire(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only HOLD status can be expired");

        verify(seatHoldRepository).findById(10L);
        verify(seatHoldRepository, never()).save(any());
    }

    // ============= EXPIRE ALL TESTS =============

    @Test
    void shouldExpireAllExpiredHolds() {
        // Given
        var hold1 = SeatHold.builder()
                .id(10L)
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().minusMinutes(5))
                .build();

        var hold2 = SeatHold.builder()
                .id(11L)
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().minusMinutes(3))
                .build();

        var hold3 = SeatHold.builder()
                .id(12L)
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(5)) // Still valid
                .build();

        when(seatHoldRepository.findAll()).thenReturn(List.of(hold1, hold2, hold3));
        when(seatHoldRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.expireAll();

        // Then
        verify(seatHoldRepository).findAll();
        verify(seatHoldRepository).saveAll(argThat(
                (List<SeatHold> list) ->
                        list != null &&
                                list.size() == 2 &&
                                list.stream().allMatch(sh -> sh.getStatus() == SeatHoldStatus.EXPIRED)
        ));
    }

    @Test
    void shouldNotSaveWhenNoExpiredHolds() {
        // Given
        var hold1 = SeatHold.builder()
                .id(10L)
                .status(SeatHoldStatus.HOLD)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        when(seatHoldRepository.findAll()).thenReturn(List.of(hold1));

        // When
        service.expireAll();

        // Then
        verify(seatHoldRepository).findAll();
        verify(seatHoldRepository, never()).saveAll(any());
    }

    @Test
    void shouldNotExpireAlreadyExpiredHolds() {
        // Given
        var hold1 = SeatHold.builder()
                .id(10L)
                .status(SeatHoldStatus.EXPIRED)
                .expiresAt(OffsetDateTime.now().minusMinutes(10))
                .build();

        when(seatHoldRepository.findAll()).thenReturn(List.of(hold1));

        // When
        service.expireAll();

        // Then
        verify(seatHoldRepository).findAll();
        verify(seatHoldRepository, never()).saveAll(any());
    }
}
