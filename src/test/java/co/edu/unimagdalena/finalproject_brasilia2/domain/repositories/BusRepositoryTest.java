package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BusRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private BusRepository busRepository;

    private Bus bus1;
    private Bus bus2;
    private Bus bus3;
    private Bus bus4;

    @BeforeEach
    void setUp() {
        busRepository.deleteAll();

        // Create buses
        bus1 = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .amenities(Set.of("WiFi", "AC", "TV"))
                .status(true)
                .build();

        bus2 = Bus.builder()
                .plate("XYZ789")
                .capacity(50)
                .amenities(Set.of("WiFi", "AC", "Reclining Seats"))
                .status(true)
                .build();

        bus3 = Bus.builder()
                .plate("DEF456")
                .capacity(30)
                .amenities(Set.of("AC"))
                .status(false)
                .build();

        bus4 = Bus.builder()
                .plate("GHI789")
                .capacity(45)
                .amenities(Set.of("WiFi", "AC", "USB Charging"))
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Bus: find by plate")
    void shouldFindByPlate() {
        // Given
        busRepository.save(bus1);
        busRepository.save(bus2);

        // When
        var result = busRepository.findByPlate("ABC123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCapacity()).isEqualTo(40);
        assertThat(result.get().isStatus()).isTrue();
        assertThat(result.get().getAmenities()).containsExactlyInAnyOrder("WiFi", "AC", "TV");
    }

    @Test
    @DisplayName("Bus: find by plate and id")
    void shouldFindByPlateAndId() {
        // Given
        bus1 = busRepository.save(bus1);

        // When
        var result = busRepository.findByPlateAndId("ABC123", bus1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(bus1.getId());
        assertThat(result.get().getPlate()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("Bus: find by status with pagination")
    void shouldFindByStatus() {
        // Given
        busRepository.save(bus1);
        busRepository.save(bus2);
        busRepository.save(bus3);
        busRepository.save(bus4);

        // When
        var activeResult = busRepository.findByStatus(true, PageRequest.of(0, 10));
        var inactiveResult = busRepository.findByStatus(false, PageRequest.of(0, 10));

        // Then
        assertThat(activeResult.getContent()).hasSize(3);
        assertThat(activeResult.getContent())
                .extracting(Bus::getPlate)
                .containsExactlyInAnyOrder("ABC123", "XYZ789", "GHI789");

        assertThat(inactiveResult.getContent()).hasSize(1);
        assertThat(inactiveResult.getContent().get(0).getPlate()).isEqualTo("DEF456");
    }

    @Test
    @DisplayName("Bus: check if exists by plate")
    void shouldCheckExistsByPlate() {
        // Given
        busRepository.save(bus1);

        // When
        var exists = busRepository.existsByPlate("ABC123");
        var notExists = busRepository.existsByPlate("ZZZ999");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Bus: count by status")
    void shouldCountByStatus() {
        // Given
        busRepository.save(bus1);
        busRepository.save(bus2);
        busRepository.save(bus3);
        busRepository.save(bus4);

        // When
        var activeCount = busRepository.countByStatus(true);
        var inactiveCount = busRepository.countByStatus(false);

        // Then
        assertThat(activeCount).isEqualTo(3L);
        assertThat(inactiveCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("Bus: find by capacity greater than or equal")
    void shouldFindByCapacityGreaterThanEqual() {
        // Given
        busRepository.save(bus1);
        busRepository.save(bus2);
        busRepository.save(bus3);
        busRepository.save(bus4);

        // When
        var result = busRepository.findByCapacityGreaterThanEqual(40, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Bus::getPlate)
                .containsExactlyInAnyOrder("ABC123", "XYZ789", "GHI789");
    }

    @Test
    @DisplayName("Bus: find by capacity less than or equal")
    void shouldFindByCapacityLessThanEqual() {
        // Given
        busRepository.save(bus1);
        busRepository.save(bus2);
        busRepository.save(bus3);
        busRepository.save(bus4);

        // When
        var result = busRepository.findByCapacityLessThanEqual(40, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Bus::getPlate)
                .containsExactlyInAnyOrder("ABC123", "DEF456");
    }

    @Test
    @DisplayName("Bus: find by capacity between")
    void shouldFindByCapacityBetween() {
        // Given
        busRepository.save(bus1);
        busRepository.save(bus2);
        busRepository.save(bus3);
        busRepository.save(bus4);

        // When
        var result = busRepository.findByCapacityBetween(35, 45, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Bus::getPlate)
                .containsExactlyInAnyOrder("ABC123", "GHI789");
    }

    @Test
    @DisplayName("Bus: return empty when plate not found")
    void shouldReturnEmptyWhenPlateNotFound() {
        // Given
        busRepository.save(bus1);

        // When
        var result = busRepository.findByPlate("NOTFOUND");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Bus: return empty when plate and id combination not found")
    void shouldReturnEmptyWhenPlateAndIdNotMatch() {
        // Given
        bus1 = busRepository.save(bus1);

        // When
        var result = busRepository.findByPlateAndId("ABC123", 999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Bus: return empty page when no buses match status")
    void shouldReturnEmptyPageWhenNoStatusMatch() {
        // Given
        busRepository.save(bus1);
        busRepository.save(bus2);

        // When - all buses are active, query for inactive
        var result = busRepository.findByStatus(false, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Bus: return empty page when no buses in capacity range")
    void shouldReturnEmptyPageWhenNoCapacityMatch() {
        // Given
        busRepository.save(bus1);
        busRepository.save(bus2);

        // When
        var result = busRepository.findByCapacityBetween(60, 80, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }
}