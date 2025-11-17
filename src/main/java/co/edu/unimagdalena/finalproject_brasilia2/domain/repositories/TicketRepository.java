package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Ticket;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByPassengerId(Long passengerId);
    Optional<Ticket> findByTripAndSeatNumber(Trip trip, String seatNumber);
    List<Ticket> findByTripId(Long tripId);
    Optional<Ticket> findByQrCode(String qrCode);
    Page<Ticket> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    @Query("""
    SELECT SUM (T.price)
    FROM Ticket T
    WHERE T.passenger.id = :passengerId AND T.status = 'SOLD'
""")
    BigDecimal totalPriceByPassengerId(@Param("passengerId") Long passengerId);

    @Query("""
        SELECT T
        FROM Ticket T
        WHERE (:fromStopId IS NULL OR T.fromStop.id = :fromStopId) AND (:toStopId IS NULL OR T.toStop.id = :toStopId)
    """)
    Page<Ticket> findAllBetweenOptionalStops(@Param("fromStopId") Long fromStopId, @Param("toStopId") Long toStopId, Pageable pageable);

    //figuro hacer esto
    @Query("""
    SELECT t FROM Ticket t
    WHERE t.status = 'SOLD'
    AND t.trip.departureTime < :threshold
""")
    List<Ticket> findNoShows(@Param("threshold") OffsetDateTime threshold);

    // Valida si hay solapamiento de tramos: dos rangos [a,b] y [c,d] se solapan si NO (b <= c OR a >= d)
    @Query("""
    SELECT COUNT(t) > 0 
    FROM Ticket t
    WHERE t.trip.id = :tripId
    AND t.seatNumber = :seatNumber
    AND t.status != 'CANCELLED'
    AND NOT (t.toStop.order <= :fromOrder OR t.fromStop.order >= :toOrder)
""")
    boolean existsOverlappingTicket(@Param("tripId") Long tripId,
                                     @Param("seatNumber") String seatNumber,
                                     @Param("fromOrder") Integer fromOrder,
                                     @Param("toOrder") Integer toOrder);
}
