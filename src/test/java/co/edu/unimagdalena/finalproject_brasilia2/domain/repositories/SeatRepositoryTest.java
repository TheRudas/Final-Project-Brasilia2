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
    private Seat seatA1;
    private Seat seatA2;
    private Seat seatB1;
    private Seat seatC1;

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
        busRepository.save(bus1);

        bus2 = Bus.builder()
                .plate("XYZ789")
                .capacity(30)
                .status(true)
                .build();
        busRepository.save(bus2);

        // Create seats for bus1
        seatA1 = Seat.builder()
                .bus(bus1)
                .number("A1")
                .seatType(SeatType.STANDARD)
                .build();

        seatA2 = Seat.builder()
                .bus(bus1)
                .number("A2")
                .seatType(SeatType.STANDARD)
                .build();

        seatB1 = Seat.builder()
                .bus(bus1)
                .number("B1")
                .seatType(SeatType.PREFERENTIAL)
                .build();

        // Create seat for bus2
        seatC1 = Seat.builder()
                .bus(bus2)
                .number("C1")
                .seatType(SeatType.STANDARD)
                .build();
    }

    @Test
    @DisplayName("Seat: find all by bus id")
    void shouldFindByBusId() {
        // Given
        seatRepository.save(seatA1);
        seatRepository.save(seatA2);
        seatRepository.save(seatB1);
        seatRepository.save(seatC1);

        // When
        var result = seatRepository.findByBusId(bus1.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Seat::getNumber)
                .containsExactlyInAnyOrder("A1", "A2", "B1");
    }

    @Test
    @DisplayName("Seat: find by bus id and seat type")
    void shouldFindByBusIdAndSeatType() {
        // Given
        seatRepository.save(seatA1);
        seatRepository.save(seatA2);
        seatRepository.save(seatB1);

        // When
        var standardSeats = seatRepository.findByBusIdAndSeatType(bus1.getId(), SeatType.STANDARD);
        var preferentialSeats = seatRepository.findByBusIdAndSeatType(bus1.getId(), SeatType.PREFERENTIAL);

        // Then
        assertThat(standardSeats).hasSize(2);
        assertThat(standardSeats)
                .extracting(Seat::getNumber)
                .containsExactlyInAnyOrder("A1", "A2");

        assertThat(preferentialSeats).hasSize(1);
        assertThat(preferentialSeats.get(0).getNumber()).isEqualTo("B1");
    }

    @Test
    @DisplayName("Seat: find by bus id and seat number")
    void shouldFindByBusIdAndNumber() {
        // Given
        seatRepository.save(seatA1);
        seatRepository.save(seatA2);

        // When
        var result = seatRepository.findByBusIdAndNumber(bus1.getId(), "A1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNumber()).isEqualTo("A1");
        assertThat(result.get().getSeatType()).isEqualTo(SeatType.STANDARD);
    }

    @Test
    @DisplayName("Seat: count seats by bus id")
    void shouldCountByBusId() {
        // Given
        seatRepository.save(seatA1);
        seatRepository.save(seatA2);
        seatRepository.save(seatB1);
        seatRepository.save(seatC1);

        // When
        var countBus1 = seatRepository.countByBusId(bus1.getId());
        var countBus2 = seatRepository.countByBusId(bus2.getId());

        // Then
        assertThat(countBus1).isEqualTo(3);
        assertThat(countBus2).isEqualTo(1);
    }

    @Test
    @DisplayName("Seat: check if seat exists by bus id and number")
    void shouldCheckExistsByBusIdAndNumber() {
        // Given
        seatRepository.save(seatA1);

        // When
        var exists = seatRepository.existsByBusIdAndNumber(bus1.getId(), "A1");
        var notExists = seatRepository.existsByBusIdAndNumber(bus1.getId(), "Z9");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Seat: find by bus id ordered by number ascending")
    void shouldFindByBusIdOrderByNumberAsc() {
        // Given
        seatRepository.save(seatB1);
        seatRepository.save(seatA1);
        seatRepository.save(seatA2);

        // When
        var result = seatRepository.findByBusIdOrderByNumberAsc(bus1.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Seat::getNumber)
                .containsExactly("A1", "A2", "B1");
    }

    @Test
    @DisplayName("Seat: return empty when bus has no seats")
    void shouldReturnEmptyWhenBusHasNoSeats() {
        // Given - bus2 has no seats saved

        // When
        var result = seatRepository.findByBusId(bus2.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Seat: return empty optional when seat not found")
    void shouldReturnEmptyWhenSeatNotFound() {
        // Given
        seatRepository.save(seatA1);

        // When
        var result = seatRepository.findByBusIdAndNumber(bus1.getId(), "Z99");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Seat: count returns zero if bus has no seats")
    void shouldReturnZeroCountForEmptyBus() {
        // Given - no seats saved for bus1

        // When
        var count = seatRepository.countByBusId(bus1.getId());

        // Then
        assertThat(count).isZero();
    }
}