package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.CashClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashClosureRepository extends JpaRepository<CashClosure, Long> {

    List<CashClosure> findByClerkId(Long clerkId);

    Optional<CashClosure> findByClerkIdAndClosureDate(Long clerkId, LocalDate date);

    List<CashClosure> findByClosureDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByClerkIdAndClosureDate(Long clerkId, LocalDate date);
}