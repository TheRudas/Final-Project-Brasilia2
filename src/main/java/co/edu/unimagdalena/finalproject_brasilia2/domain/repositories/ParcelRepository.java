package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Parcel;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
  List<Parcel> FindBySenderId(Long senderId);
  List<Parcel> FindByReceiverId(Long receiverId);
  List<Parcel> FindByTripId(Long tripId);
  List<Parcel> FindByStatus(ParcelStatus status);
}
