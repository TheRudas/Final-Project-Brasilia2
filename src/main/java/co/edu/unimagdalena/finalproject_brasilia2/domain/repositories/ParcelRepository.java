package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Parcel;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    List<Parcel> findBySenderName(String senderName);
    List<Parcel> findBySenderPhone(String senderPhone);
    List<Parcel> findByReceiverName(String receiverName);
    List<Parcel> findByReceiverPhone(String receiverPhone);
    List<Parcel> findByStatus(ParcelStatus status);
    List<Parcel> findByFromStopId(Long fromStopId);
    List<Parcel> findByToStopId(Long toStopId);

    // ⭐ AGREGAR ESTAS QUERIES

    /**
     * Buscar paquete por código (único)
     */
    Optional<Parcel> findByCode(String code);

    /**
     * Buscar paquetes para entregar en una parada
     */
    @Query("""
        SELECT p FROM Parcel p
        WHERE p.toStop.id = :stopId
        AND p.status = :status
    """)
    List<Parcel> findByToStopIdAndStatus(@Param("stopId") Long stopId, @Param("status") ParcelStatus status);

    /**
     * Contar paquetes por estado
     */
    Long countByStatus(ParcelStatus status);

    /**
     * Verificar si existe un paquete con ese código
     */
    boolean existsByCode(String code);
}