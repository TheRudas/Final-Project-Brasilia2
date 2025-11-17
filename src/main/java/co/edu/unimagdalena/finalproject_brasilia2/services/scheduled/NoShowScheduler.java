package co.edu.unimagdalena.finalproject_brasilia2.services.scheduled;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Ticket;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoShowScheduler {

    private final TicketRepository ticketRepository;

    // Este es un esclavo que busca tickets vendidos cuyo viaje va a salir en t-5 y los marca NO_SHOW automáticamente. Gracias esclavo

    @Scheduled(fixedRate = 60000) // 60k ms = 1 min
    @Transactional
    public void markNoShows() {
        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(5);

        List<Ticket> noShows = ticketRepository.findNoShows(threshold);

        if (!noShows.isEmpty()) {
            noShows.forEach(ticket -> {
                log.info("Marking ticket {} as NO_SHOW for trip {} (departure: {})",
                        ticket.getId(),
                        ticket.getTrip().getId(),
                        ticket.getTrip().getDepartureTime());

                ticket.setStatus(TicketStatus.NO_SHOW);
            });

            ticketRepository.saveAll(noShows);
            log.info("Marked {} tickets as NO_SHOW", noShows.size());
        }
    }
}