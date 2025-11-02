package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Baggage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaggageRepository extends JpaRepository<Baggage,Long> {
}
