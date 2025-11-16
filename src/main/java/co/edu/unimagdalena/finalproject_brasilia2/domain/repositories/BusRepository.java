package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByPlate(String plate);

    Optional<Bus> findByPlateAndId(String plate, Long id);

    Page<Bus> findByStatus(boolean status, Pageable pageable);

    boolean existsByPlate(String plate);

    Long countByStatus(boolean status);

    Long id(Long id);

    Page<Bus> findByCapacityGreaterThanEqual(Integer capacityIsGreaterThan, Pageable pageable);

    Page<Bus> findByCapacityLessThanEqual(Integer capacityIsLessThan, Pageable pageable);

    Page<Bus> findByCapacityBetween(Integer capacityAfter, Integer capacityBefore, Pageable pageable);
}
