package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Seat;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByBusId(Long busId);
    List<Seat> findByBusIdAndSeatType(Long busId, SeatType seatType);
    Optional<Seat> findByBusIdAndNumber(Long busId, String seatNumber);
    Long countByBusId(Long busId);
    boolean existsByBusIdAndNumber(Long busId, String seatNumber);
    List<Seat> findByBusIdOrderByNumberAsc(Long busId);
}
