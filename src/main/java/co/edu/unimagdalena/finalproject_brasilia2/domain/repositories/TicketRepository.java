package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
}
