package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.SeatHold;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatHoldService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.SeatHoldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {

    private final SeatHoldRepository seatHoldRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final SeatHoldMapper mapper;


    @Override
    @Transactional
    public SeatHoldResponse create(SeatHoldCreateRequest request) {
        // trip y user existe
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(request.tripId())));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User %d not found".formatted(request.userId())));

        if(seatRepository.findByBusIdAndNumber(trip.getBus().getId(), request.seatNumber()).isEmpty()) {
            throw new NotFoundException("Seat %s not found in bus %s".formatted(request.seatNumber(), trip.getBus().getPlate()));
        }

        // checkear si el asiento no ha sido vendido (si ya existe un ticket para ese asiento, error)
        if (ticketRepository.findByTripAndSeatNumber(trip, request.seatNumber()).isPresent()) {
            throw new IllegalStateException("Seat %s already sold for this trip".formatted(request.seatNumber()));
        }

        // checkear si el asiento esta siendo reservado por alguien ma (active hold)
        List<SeatHold> existingHolds = seatHoldRepository.findByTripId(request.tripId());
        boolean isHeld = existingHolds.stream()
                .anyMatch(hold -> hold.getSeatNumber().equals(request.seatNumber())
                        && hold.getStatus() == SeatHoldStatus.HOLD
                        && hold.getExpiresAt().isAfter(OffsetDateTime.now()));

        if (isHeld) {
            throw new IllegalStateException("Seat %s is currently held".formatted(request.seatNumber()));
        }

        /*Resumen: Existe el viaje? Existe el usuario? El asiento está libre? Nadie lo tiene reservado?
          Entonces messirve, reserva por 10 min*/

        SeatHold seatHold = mapper.toEntity(request);
        seatHold.setTrip(trip);
        seatHold.setUser(user);
        seatHold.setExpiresAt(OffsetDateTime.now().plusMinutes(10));

        SeatHold saved = seatHoldRepository.save(seatHold);

        return mapper.toResponse(saved);
    }

    @Override
    public SeatHoldResponse get(Long id) {
        return seatHoldRepository.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("SeatHold %d not found".formatted(id)));
    }

    @Override
    public List<SeatHoldResponse> listByTripId(Long tripId) {
        List<SeatHold> holds = seatHoldRepository.findByTripId(tripId);
        if (holds.isEmpty()) {
            throw new NotFoundException("No holds found for trip %d".formatted(tripId));
        }
        return holds.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<SeatHoldResponse> listByUserId(Long userId) {
        List<SeatHold> holds = seatHoldRepository.findByUserId(userId);
        if (holds.isEmpty()) {
            throw new NotFoundException("No holds found for user %d".formatted(userId));
        }
        return holds.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void expire(Long id) {
        SeatHold hold = seatHoldRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SeatHold %d not found".formatted(id)));

        if (hold.getStatus() != SeatHoldStatus.HOLD) {
            throw new IllegalStateException("Only HOLD status can be expired");
        }

        hold.setStatus(SeatHoldStatus.EXPIRED);
        seatHoldRepository.save(hold);
    }

    @Override
    @Transactional
    public void expireAll() {
        // Usar query optimizada del repositorio en lugar de findAll()
        OffsetDateTime now = OffsetDateTime.now();
        List<SeatHold> expiredHolds = seatHoldRepository.findByStatusAndExpiresAtBefore(SeatHoldStatus.HOLD, now);

        if (!expiredHolds.isEmpty()) {
            expiredHolds.forEach(hold -> hold.setStatus(SeatHoldStatus.EXPIRED));
            seatHoldRepository.saveAll(expiredHolds);
        }
    }
}
