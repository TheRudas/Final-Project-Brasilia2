package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.SeatHold;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatHoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    List<SeatHold> findByTripId(Long tripId);
    List<SeatHold> findByUserId(Long userId);
    List<SeatHold> findByUserIdAndTripId(Long userId, Long tripId);
    boolean existsByUserIdAndTripId(Long userId, Long tripId);
    boolean existsSeatHoldById(Long id);
    Optional<SeatHold> findByTripIdAndSeatNumberAndStatus(Long tripId, String seatNumber, SeatHoldStatus status);

    /*
      Busca holds vencidos (generada automáticamente por Spring Data JPA)
        */
    List<SeatHold> findByStatusAndExpiresAtBefore(SeatHoldStatus status, OffsetDateTime now);

    //query manual que es mas eficiente con el scheduler
    @Query("""
        SELECT sh FROM SeatHold sh
        WHERE sh.status = :status
        AND sh.expiresAt < :now
        ORDER BY sh.expiresAt ASC
    """)
    List<SeatHold> findExpiredHolds(
            @Param("status") SeatHoldStatus status,
            @Param("now") OffsetDateTime now
    );
}