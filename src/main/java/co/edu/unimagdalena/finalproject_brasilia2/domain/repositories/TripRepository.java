package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip,Long> {
    List<Trip> findByStatus(TripStatus status);
    List<Trip> findByBusId(Long busId);
    List<Trip> findByRouteId(Long routeId);

    List<Trip> findByStatusAndBusId(TripStatus status, Long busId);
    boolean existsTripByRouteId(Long routeId);

}
