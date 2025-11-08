package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {
    List<Bus> findBusById(Long BusId);
    List<Bus> FindByPLateAndID(Long Id, String Plate);
    List<Bus> FindByPLate(String Plate);
    List<Bus> FindByStatus(String Status);
    boolean existsByPlate(String plate);
    boolean existsById(Long id);
    Long countByStatus(boolean status);
}
