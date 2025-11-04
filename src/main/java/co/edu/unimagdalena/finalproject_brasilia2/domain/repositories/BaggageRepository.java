package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Baggage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BaggageRepository extends JpaRepository<Baggage,Long> {
    Page<Baggage> findByWeightKgGreaterThanEqual(BigDecimal weightKg, Pageable pageable);
    Page<Baggage> findByWeightKgLessThanEqual(BigDecimal weightKg, Pageable pageable);
    Page<Baggage> findByWeightKgBetween(BigDecimal startKg, BigDecimal endKg, Pageable pageable);
    Optional<Baggage> findByTagCode(String tagCode);
    List<Baggage> findByTicket_Passenger_Id(Long passengerId);
}
