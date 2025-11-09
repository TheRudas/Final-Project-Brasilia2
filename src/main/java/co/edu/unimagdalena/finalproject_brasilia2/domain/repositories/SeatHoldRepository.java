package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.SeatHold;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    List<SeatHold> findByTripId(Long tripId);
    List<SeatHold> findByUserId(Long userId);
    List<SeatHold> findByUserIdAndTripId(Long userId, Long tripId);
    boolean existsByUserIdAndTripId(Long userId, Long tripId);
    boolean existsSeatHoldById(Long id);
}
