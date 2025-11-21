package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    Page<Assignment> findByDriverId(Long driverId, Pageable pageable);
    Page<Assignment> findByTripId(Long tripId,Pageable pageable);

    Optional<Assignment> findById(Long id);
    long countByDriverId(Long driverId);

    long countByCheckListOk(boolean checkListOk);
    boolean existsByDriverId(Long driverId);
    boolean existsByTripId(Long tripId);

    // Search a unique assignment for a trip
    Optional<Assignment> findFirstByTripId(Long tripId);
}
