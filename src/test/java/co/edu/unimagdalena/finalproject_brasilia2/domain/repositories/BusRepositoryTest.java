package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BusRepositoryTest extends AbstractRepositoryIT{


    @Autowired
    private BusRepository busRepository;

    private Bus bus1;
    private Bus bus2;
    private Bus bus3;

    @BeforeEach
    void setUp() {
        busRepository.deleteAll();

        bus1 = Bus.builder()
                .plate("AAA111")
                .capacity(40)
                .status(true)
                .build();

        bus2 = Bus.builder()
                .plate("BBB222")
                .capacity(45)
                .status(false)
                .build();

        bus3 = Bus.builder()
                .plate("CCC333")
                .capacity(50)
                .status(true)
                .build();

        busRepository.saveAll(List.of(bus1, bus2, bus3));
    }

    @Test
    @DisplayName("Bus: find by ID")
    void findBusById() {
        var result = busRepository.findById(bus1.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getPlate()).isEqualTo("AAA111");
    }

    @Test
    @DisplayName("Bus: find by plate and id")
    void findByPlateAndID() {
        var result = busRepository.findByPlateAndId("BBB222", bus2.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCapacity()).isEqualTo(45);
    }

    @Test
    @DisplayName("Bus: find by plate")
    void findByPlate() {
        var result = busRepository.findByPlate("CCC333");
        assertThat(result).isPresent();
        assertThat(result.get().isStatus()).isTrue();
    }

    @Test
    @DisplayName("Bus: find by status")
    void findByStatus() {
        var activeBuses = busRepository.findByStatus(true, org.springframework.data.domain.PageRequest.of(0, 10));
        var inactiveBuses = busRepository.findByStatus(false, org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(activeBuses).hasSize(2);
        assertThat(inactiveBuses).hasSize(1);
    }

    @Test
    @DisplayName("Bus: exists by plate")
    void existsByPlate() {
        boolean exists = busRepository.existsByPlate("AAA111");
        boolean notExists = busRepository.existsByPlate("ZZZ999");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Bus: exists by ID")
    void existsById() {
        boolean exists = busRepository.existsById(bus3.getId());
        boolean notExists = busRepository.existsById(999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Bus: count by status")
    void countByStatus() {
        long activeCount = busRepository.countByStatus(true);
        long inactiveCount = busRepository.countByStatus(false);

        assertThat(activeCount).isEqualTo(2);
        assertThat(inactiveCount).isEqualTo(1);
    }
}