package co.edu.unimagdalena.finalproject_brasilia2.services.scheduled;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.SeatHold;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.SeatHoldRepository;
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
public class SeatHoldExpirationScheduler {

    private final SeatHoldRepository seatHoldRepository;

    //Otro esclavo que cada 30 segundos busca reservaciones que hayan pasao su tiempo de expiracion y las expira.

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void expireHolds() {
        OffsetDateTime now = OffsetDateTime.now();

        // Buscar todos los expirados
        List<SeatHold> expiredHolds = seatHoldRepository.findAll().stream()
                .filter(hold -> hold.getStatus() == SeatHoldStatus.HOLD)
                .filter(hold -> hold.getExpiresAt().isBefore(now)).toList();

        if (!expiredHolds.isEmpty()) {
            expiredHolds.forEach(hold -> {hold.setStatus(SeatHoldStatus.EXPIRED);});

            seatHoldRepository.saveAll(expiredHolds);
        }
    }
}
