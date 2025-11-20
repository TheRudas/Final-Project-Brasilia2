package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // ==================== QUERIES EXISTENTES ====================
    List<Trip> findByStatus(TripStatus status);
    List<Trip> findByBusId(Long busId);
    List<Trip> findByRouteId(Long routeId);
    List<Trip> findByStatusAndBusId(TripStatus status, Long busId);
    boolean existsTripByRouteId(Long routeId);

    // ==================== QUERIES NUEVAS ====================

    // Buscar viajes por fecha
    List<Trip> findByDate(LocalDate date);

    // Buscar viajes por ruta y fecha
    List<Trip> findByRouteIdAndDate(Long routeId, LocalDate date);

    // Buscar viajes por ruta, fecha y estado
    List<Trip> findByRouteIdAndDateAndStatus(Long routeId, LocalDate date, TripStatus status);

    // Buscar viajes por bus y fecha
    List<Trip> findByBusIdAndDate(Long busId, LocalDate date);

    // Buscar viajes por bus, fecha y estados (para validar disponibilidad)
    List<Trip> findByBusIdAndDateAndStatusIn(Long busId, LocalDate date, List<TripStatus> statuses);

    // Buscar viajes activos (SCHEDULED, BOARDING, DEPARTED) por bus y fecha
    @Query("""
        SELECT t FROM Trip t
        WHERE t.bus.id = :busId
        AND t.date = :date
        AND t.status IN ('SCHEDULED', 'BOARDING', 'DEPARTED')
    """)
    List<Trip> findActiveTripsByBusAndDate(@Param("busId") Long busId, @Param("date") LocalDate date);

    // Buscar viajes disponibles para reservar (SCHEDULED, en el futuro)
    @Query("""
        SELECT t FROM Trip t
        WHERE t.route.id = :routeId
        AND t.date = :date
        AND t.status = 'SCHEDULED'
        AND t.departureTime > :now
        ORDER BY t.departureTime ASC
    """)
    List<Trip> findAvailableTrips(@Param("routeId") Long routeId,
                                  @Param("date") LocalDate date,
                                  @Param("now") OffsetDateTime now);

    // Buscar viajes en rango de fechas
    List<Trip> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Buscar viajes por ruta en rango de fechas
    List<Trip> findByRouteIdAndDateBetween(Long routeId, LocalDate startDate, LocalDate endDate);

    // Contar viajes por estado
    Long countByStatus(TripStatus status);

    // Contar viajes de un bus en una fecha
    Long countByBusIdAndDate(Long busId, LocalDate date);

    // Verificar si existe un viaje con ciertos criterios
    boolean existsByBusIdAndDateAndStatusIn(Long busId, LocalDate date, List<TripStatus> statuses);

    // ⭐ NUEVO: Verificar si un bus tiene trips asignados
    boolean existsByBusId(Long busId);

    // Buscar viajes próximos a partir (para recordatorios)
    @Query("""
        SELECT t FROM Trip t
        WHERE t.status = 'SCHEDULED'
        AND t.departureTime BETWEEN :start AND :end
        ORDER BY t.departureTime ASC
    """)
    List<Trip> findUpcomingTrips(@Param("start") OffsetDateTime start,
                                 @Param("end") OffsetDateTime end);

    // Buscar viajes con retraso (llegada programada pasada pero aún DEPARTED)
    @Query("""
        SELECT t FROM Trip t
        WHERE t.status = 'DEPARTED'
        AND t.arrivalTime < :now
    """)
    List<Trip> findDelayedTrips(@Param("now") OffsetDateTime now);

    // Verificar conflictos de horario para un bus (solapamiento)
    @Query("""
        SELECT t FROM Trip t
        WHERE t.bus.id = :busId
        AND t.status IN ('SCHEDULED', 'BOARDING', 'DEPARTED')
        AND t.date = :date
        AND (
            (t.departureTime <= :departureTime AND t.arrivalTime >= :departureTime)
            OR (t.departureTime <= :arrivalTime AND t.arrivalTime >= :arrivalTime)
            OR (t.departureTime >= :departureTime AND t.arrivalTime <= :arrivalTime)
        )
    """)
    List<Trip> findConflictingTrips(@Param("busId") Long busId,
                                    @Param("date") LocalDate date,
                                    @Param("departureTime") OffsetDateTime departureTime,
                                    @Param("arrivalTime") OffsetDateTime arrivalTime);

    // Buscar viajes en estado BOARDING (para alertas)
    @Query("""
        SELECT t FROM Trip t
        WHERE t.status = 'BOARDING'
        AND t.departureTime < :maxDepartureTime
        ORDER BY t.departureTime ASC
    """)
    List<Trip> findBoardingTrips(@Param("maxDepartureTime") OffsetDateTime maxDepartureTime);
}