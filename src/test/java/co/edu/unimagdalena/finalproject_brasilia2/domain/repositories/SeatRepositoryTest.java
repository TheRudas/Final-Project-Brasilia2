package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Seat;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BusRepository busRepository;

    private Bus bus1;
    private Bus bus2;
    private Seat seat1;
    private Seat seat2;
    private Seat seat3;
    private Seat seat4;

    @BeforeEach
    void setUp() {
        seatRepository.deleteAll();
        busRepository.deleteAll();

        // Create buses
        bus1 = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();
        bus1 = busRepository.save(bus1);

        bus2 = Bus.builder()
                .plate("XYZ789")
                .capacity(30)
                .status(true)
                .build();
        bus2 = busRepository.save(bus2);

        // Create seats for bus1
        seat1 = Seat.builder()
                .bus(bus1)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        seat2 = Seat.builder()
                .bus(bus1)
                .number("A2")
                .seatType(SeatType.STANDARD)
                .build();

        seat3 = Seat.builder()
                .bus(bus1)
                .number("P1")
                .seatType(SeatType.PREFERENTIAL)
                .build();

        // Create seat for bus2
        seat4 = Seat.builder()
                .bus(bus2)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();
    }

    @Test
    @DisplayName("Seat: find by bus id")
    void shouldFindByBusId() {
        // Given
        seatRepository.save(seat1);
        seatRepository.save(seat2);
        seatRepository.save(seat3);
        seatRepository.save(seat4);

        // When
        var bus1Seats = seatRepository.findByBusId(bus1.getId());
        var bus2Seats = seatRepository.findByBusId(bus2.getId());

        // Then
        assertThat(bus1Seats).hasSize(3);
        assertThat(bus1Seats)
                .extracting(Seat::getNumber)
                .containsExactlyInAnyOrder("A1", "A2", "P1");

        assertThat(bus2Seats).hasSize(1);
        assertThat(bus2Seats.get(0).getNumber()).isEqualTo("A1");
    }

    @Test
    @DisplayName("Seat: find by bus id and seat type")
    void shouldFindByBusIdAndSeatType() {
        // Given
        seatRepository.save(seat1);
        seatRepository.save(seat2);
        seatRepository.save(seat3);

        // When
        var standardSeats = seatRepository.findByBusIdAndSeatType(bus1.getId(), SeatType.STANDARD);
        var preferentialSeats = seatRepository.findByBusIdAndSeatType(bus1.getId(), SeatType.PREFERENTIAL);

        // Then
        assertThat(standardSeats).hasSize(2);
        assertThat(standardSeats)
                .extracting(Seat::getNumber)
                .containsExactlyInAnyOrder("A1", "A2");

        assertThat(preferentialSeats).hasSize(1);
        assertThat(preferentialSeats.get(0).getNumber()).isEqualTo("P1");
    }

    @Test
    @DisplayName("Seat: find by bus id and number")
    void shouldFindByBusIdAndNumber() {
        // Given
        seatRepository.save(seat1);
        seatRepository.save(seat2);

        // When
        var result = seatRepository.findByBusIdAndNumber(bus1.getId(), "A1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSeatType()).isEqualTo(SeatType.STANDARD);
    }

    @Test
    @DisplayName("Seat: count by bus id")
    void shouldCountByBusId() {
        // Given
        seatRepository.save(seat1);
        seatRepository.save(seat2);
        seatRepository.save(seat3);
        seatRepository.save(seat4);

        // When
        var bus1Count = seatRepository.countByBusId(bus1.getId());
        var bus2Count = seatRepository.countByBusId(bus2.getId());

        // Then
        assertThat(bus1Count).isEqualTo(3L);
        assertThat(bus2Count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Seat: check if exists by bus id and number")
    void shouldCheckExistsByBusIdAndNumber() {
        // Given
        seatRepository.save(seat1);

        // When
        var exists = seatRepository.existsByBusIdAndNumber(bus1.getId(), "A1");
        var notExists = seatRepository.existsByBusIdAndNumber(bus1.getId(), "Z99");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Seat: find by bus id ordered by number ascending")
    void shouldFindByBusIdOrderByNumberAsc() {
        // Given
        seatRepository.save(seat3); // P1
        seatRepository.save(seat1); // A1
        seatRepository.save(seat2); // A2

        // When
        var result = seatRepository.findByBusIdOrderByNumberAsc(bus1.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Seat::getNumber)
                .containsExactly("A1", "A2", "P1"); // Ordered alphabetically
    }

    @Test
    @DisplayName("Seat: return empty list when bus has no seats")
    void shouldReturnEmptyWhenBusHasNoSeats() {
        // Given - bus without seats
        Bus bus3 = Bus.builder()
                .plate("DEF456")
                .capacity(20)
                .status(true)
                .build();
        bus3 = busRepository.save(bus3);

        // When
        var result = seatRepository.findByBusId(bus3.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Seat: return empty when bus id and number not found")
    void shouldReturnEmptyWhenBusIdAndNumberNotFound() {
        // Given
        seatRepository.save(seat1);

        // When
        var result = seatRepository.findByBusIdAndNumber(bus1.getId(), "Z99");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Seat: return empty list when bus has no seats of specific type")
    void shouldReturnEmptyWhenBusHasNoSeatsOfType() {
        // Given
        seatRepository.save(seat1); // Only standard seat

        // When
        var result = seatRepository.findByBusIdAndSeatType(bus1.getId(), SeatType.PREFERENTIAL);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Seat: return zero count when bus has no seats")
    void shouldReturnZeroCountWhenBusHasNoSeats() {
        // Given - bus without seats
        Bus bus3 = Bus.builder()
                .plate("DEF456")
                .capacity(20)
                .status(true)
                .build();
        bus3 = busRepository.save(bus3);

        // When
        var count = seatRepository.countByBusId(bus3.getId());

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("Seat: verify seat type is properly stored")
    void shouldVerifySeatTypeIsProperlyStored() {
        // Given
        seatRepository.save(seat1);
        seatRepository.save(seat3);

        // When
        var standardSeat = seatRepository.findByBusIdAndNumber(bus1.getId(), "A1");
        var preferentialSeat = seatRepository.findByBusIdAndNumber(bus1.getId(), "P1");

        // Then
        assertThat(standardSeat).isPresent();
        assertThat(standardSeat.get().getSeatType()).isEqualTo(SeatType.STANDARD);

        assertThat(preferentialSeat).isPresent();
        assertThat(preferentialSeat.get().getSeatType()).isEqualTo(SeatType.PREFERENTIAL);
    }

    @Test
    @DisplayName("Seat: same seat number in different buses")
    void shouldAllowSameSeatNumberInDifferentBuses() {
        // Given
        seatRepository.save(seat1); // A1 in bus1
        seatRepository.save(seat4); // A1 in bus2

        // When
        var bus1SeatA1 = seatRepository.findByBusIdAndNumber(bus1.getId(), "A1");
        var bus2SeatA1 = seatRepository.findByBusIdAndNumber(bus2.getId(), "A1");

        // Then
        assertThat(bus1SeatA1).isPresent();
        assertThat(bus2SeatA1).isPresent();
        assertThat(bus1SeatA1.get().getBus().getPlate()).isEqualTo("ABC123");
        assertThat(bus2SeatA1.get().getBus().getPlate()).isEqualTo("XYZ789");
    }
}