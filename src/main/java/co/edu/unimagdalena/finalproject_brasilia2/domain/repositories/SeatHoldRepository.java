package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.SeatHold;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatHoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    List<SeatHold> findByTripId(Long tripId);
    List<SeatHold> findByUserId(Long userId);
    List<SeatHold> findByUserIdAndTripId(Long userId, Long tripId);
    List<SeatHold> findByStatusAndExpiresAtBefore(SeatHoldStatus status, OffsetDateTime now);
    boolean existsByUserIdAndTripId(Long userId, Long tripId);
    boolean existsSeatHoldById(Long id);
}
