package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {
    List<Bus> findByPlate(String plate);

    List<Bus> findByPlateAndId(String plate, Long id);

    List<Bus> findByStatus(boolean status);

    boolean existsByPlate(String plate);

    Long countByStatus(boolean status);
}
