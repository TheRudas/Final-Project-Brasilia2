package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Parcel;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    List<Parcel> findBySenderName(String senderName);

    List<Parcel> findBySenderPhone(String senderPhone);

    List<Parcel> findByReceiverName(String receiverName);

    List<Parcel> findByReceiverPhone(String receiverPhone);

    List<Parcel> findByStatus(ParcelStatus status);

    List<Parcel> findByFromStopId(Long fromStopId);

    List<Parcel> findByToStopId(Long toStopId);
}
