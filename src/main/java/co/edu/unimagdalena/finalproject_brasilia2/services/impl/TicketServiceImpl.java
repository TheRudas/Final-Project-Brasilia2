package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.TicketService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final StopRepository stopRepository;
    private final TicketMapper mapper;

    @Override
    @Transactional
    public TicketResponse create(TicketCreateRequest request) {
        // validate trip and passengers exists
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(request.tripId())));

        User passenger = userRepository.findById(request.passengerId())
                .orElseThrow(() -> new NotFoundException("Passenger %d not found".formatted(request.passengerId())));

        //validate stops exist and belong to trip's route to prevent jokers
        Stop fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new NotFoundException("FromStop %d not found".formatted(request.fromStopId())));

        Stop toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new NotFoundException("ToStop %d not found".formatted(request.toStopId())));
        //belong

        if (!fromStop.getRoute().getId().equals(trip.getRoute().getId())) {
            throw new IllegalStateException("FromStop doesn't belong to trip's route");
        }
        if (!toStop.getRoute().getId().equals(trip.getRoute().getId())) {
            throw new IllegalStateException("ToStop does not belong to trip's route");
        }

        // 4. ya me canse del ingles, verificar orden y disponibilidad de asientos para ese viaje
        if (fromStop.getOrder() >= toStop.getOrder()) {
            throw new IllegalStateException("FromStop order must be less than ToStop order");
        }
        if (ticketRepository.findByTripAndSeatNumber(trip, request.seatNumber()).isPresent()) {
            throw new IllegalStateException("Seat %s already sold for this trip".formatted(request.seatNumber()));
        }

        // Crear ticket
        Ticket ticket = mapper.toEntity(request);
        ticket.setTrip(trip);
        ticket.setPassenger(passenger);
        ticket.setFromStop(fromStop);
        ticket.setToStop(toStop);
        ticket.setQrCode(generateQrCode());

        return mapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse update(Long id, TicketUpdateRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));

        // Validate seat change if provided
        if (request.seatNumber() != null && !request.seatNumber().equals(ticket.getSeatNumber())) {
            if (ticketRepository.findByTripAndSeatNumber(ticket.getTrip(), request.seatNumber()).isPresent()) {
                throw new IllegalStateException("Seat %s already occupied".formatted(request.seatNumber()));
            }
        }

        mapper.patch(ticket, request);
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponse get(Long id) {
        return ticketRepository.findById(id).map(mapper::toResponse).orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
    }

    @Override
    public TicketResponse getByQrCode(String qrCode) {
        return ticketRepository.findByQrCode(qrCode).map(mapper::toResponse).orElseThrow(() ->
                new NotFoundException("Ticket with QR %s not found".formatted(qrCode)));
    }

    @Override
    public List<TicketResponse> getByPassengerId(Long passengerId) {
        List<Ticket> tickets = ticketRepository.findByPassengerId(passengerId);
        if (tickets.isEmpty()) {
            throw new NotFoundException("No tickets found for passenger %d".formatted(passengerId));
        }
        return tickets.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<TicketResponse> getByTripId(Long tripId) {
        List<Ticket> tickets = ticketRepository.findByTripId(tripId);
        if (tickets.isEmpty()) {
            throw new NotFoundException("No tickets found for trip %d".formatted(tripId));
        }
        return tickets.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Page<TicketResponse> getByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable) {
        Page<Ticket> tickets = ticketRepository.findByPaymentMethod(paymentMethod, pageable);
        if (tickets.isEmpty()) {
            throw new NotFoundException("No tickets found with payment method %s".formatted(paymentMethod));
        }
        return tickets.map(mapper::toResponse);
    }

    @Override
    public Page<TicketResponse> getByStatus(TicketStatus status, Pageable pageable) {
        Page<Ticket> tickets = ticketRepository.findByStatus(status, pageable);
        if (tickets.isEmpty()) {
            throw new NotFoundException("No tickets found with status %s".formatted(status));
        }
        return tickets.map(mapper::toResponse);
    }

    @Override
    public Page<TicketResponse> getBetweenStops(Long fromStopId, Long toStopId, Pageable pageable) {
        Page<Ticket> tickets = ticketRepository.findAllBetweenOptionalStops(fromStopId, toStopId, pageable);
        if (tickets.isEmpty()) {
            throw new NotFoundException("No tickets found between stops");
        }
        return tickets.map(mapper::toResponse);
    }

    @Override
    public BigDecimal getTotalByPassengerId(Long passengerId) {
        BigDecimal total = ticketRepository.totalPriceByPassengerId(passengerId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public TicketResponse cancel(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));

        if (ticket.getStatus() != TicketStatus.SOLD) {
            throw new IllegalStateException("Only SOLD tickets can be cancelled");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        ticketRepository.delete(ticket);
    }

    private String generateQrCode() {
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
