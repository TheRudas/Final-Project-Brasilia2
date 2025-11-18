package co.edu.unimagdalena.finalproject_brasilia2.services.scheduled;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Ticket;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalproject_brasilia2.services.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoShowScheduler {

    private final TicketRepository ticketRepository;
    private final ConfigService configService;

    /* Este es un esclavo que busca tickets vendidos cuyo viaje va a salir en t-5 y los marca NO_SHOW automáticamente. Gracias esclavo
    Los logs vienen en Simple Logging Facade 4 java, dice que sirven para auditorias y que es bueno dejarlos en los schedulers, se generan cada vez que
    se marca el ticket como no show y obtener detalles.

    VENTA RÁPIDA: Al marcar NO_SHOW, la silla se libera automaticamente porque existsOverlappingTicket() solo cuenta status = SOLD.
    Esto permite vender la silla a último momento si alguien llega a la terminal.
    */

    @Scheduled(fixedRate = 60000) // 60k ms = 1 min
    @Transactional
    public void markNoShows() {
        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(5);
        List<Ticket> noShows = ticketRepository.findNoShows(threshold);

        if (!noShows.isEmpty()) {
            BigDecimal noShowFee = configService.getValue("NO_SHOW_FEE");

            noShows.forEach(ticket -> {
                log.info("Marking ticket {} as NO_SHOW for trip {} (departure: {}). Seat {} now available for quick sale. Fee: {}",
                        ticket.getId(),
                        ticket.getTrip().getId(),
                        ticket.getTrip().getDepartureTime(),
                        ticket.getSeatNumber(),
                        noShowFee);

                ticket.setStatus(TicketStatus.NO_SHOW);
                ticket.setNoShowFee(noShowFee);
            });

            ticketRepository.saveAll(noShows);
            log.info("Marked {} tickets as NO_SHOW with fee {}. Seats released for quick sale.", noShows.size(), noShowFee);
        }
    }
}