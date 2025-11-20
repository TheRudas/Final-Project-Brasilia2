package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Assignment;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Ticket;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.AssignmentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.TripService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.TripMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private static final Logger log = LoggerFactory.getLogger(TripServiceImpl.class);

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final TicketRepository ticketRepository;
    private final AssignmentRepository assignmentRepository;
    private final TripMapper mapper;


            // ========================= CREATE =========================
            @Override
            @Transactional
            public TripResponse create(TripCreateRequest request) {

                Route route = routeRepository.findById(request.routeId())
                        .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(request.routeId())));

                Bus bus = busRepository.findById(request.busId())
                        .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(request.busId())));

                // Validar que el bus esté activo
                if (!bus.isStatus()) {
                    throw new IllegalStateException(
                            "Bus %s is not active/operational".formatted(bus.getPlate())
                    );
                }

                // Usar el mapper para conversión (maneja zona horaria automáticamente)
                Trip trip = mapper.toEntity(request);
                trip.setRoute(route);
                trip.setBus(bus);
                trip.setStatus(TripStatus.SCHEDULED);

                // Validar que la fecha no esté en el pasado
                if (trip.getDate().isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException(
                            "Trip date cannot be in the past: %s".formatted(trip.getDate())
                    );
                }

                // Validar que arrival sea después de departure
                if (!trip.getArrivalTime().isAfter(trip.getDepartureTime())) {
                    throw new IllegalArgumentException("Arrival time must be after departure time");
                }

                // Validar que no haya conflictos de horario
                List<Trip> conflicts = tripRepository.findConflictingTrips(
                        bus.getId(),
                        trip.getDate(),
                        trip.getDepartureTime(),
                        trip.getArrivalTime()
                );

                if (!conflicts.isEmpty()) {
                    throw new IllegalStateException(
                            "Bus %s has conflicting trips on %s".formatted(bus.getPlate(), trip.getDate())
                    );
                }

                log.info("Creating trip: route={}, bus={}, date={}, departure={}, arrival={}",
                        route.getId(), bus.getPlate(), trip.getDate(),
                        trip.getDepartureTime(), trip.getArrivalTime());

                return mapper.toTripResponse(tripRepository.save(trip));
            }


            // ========================= UPDATE =========================
            @Override
            @Transactional
            public TripResponse update(Long id, TripUpdateRequest request) {

                Trip trip = tripRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));

                // No permitir actualizar viajes completados o cancelados
                if (trip.getStatus() == TripStatus.ARRIVED || trip.getStatus() == TripStatus.CANCELLED) {
                    throw new IllegalStateException(
                            "Cannot update a trip with status %s".formatted(trip.getStatus())
                    );
                }

                // No permitir actualizar viajes que ya salieron
                if (trip.getStatus() == TripStatus.DEPARTED) {
                    throw new IllegalStateException("Cannot update a trip that has already departed");
                }

                // Route update if present
                if (request.routeId() != null) {
                    Route route = routeRepository.findById(request.routeId())
                            .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(request.routeId())));
                    trip.setRoute(route);
                }

                // Bus update if present (con validación de estado activo)
                if (request.busId() != null) {
                    Bus bus = busRepository.findById(request.busId())
                            .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(request.busId())));

                    if (!bus.isStatus()) {
                        throw new IllegalStateException(
                                "Cannot assign inactive bus %s".formatted(bus.getPlate())
                        );
                    }

                    trip.setBus(bus);
                }

                // Aplicar actualizaciones parciales usando el mapper
                mapper.patch(trip, request);

                // Validar coherencia de horarios después de actualizar
                if (trip.getArrivalTime() != null && trip.getDepartureTime() != null) {
                    if (!trip.getArrivalTime().isAfter(trip.getDepartureTime())) {
                        throw new IllegalArgumentException(
                                "Arrival time must be after departure time"
                        );
                    }
                }

                // Validar conflictos de horario si se cambió fecha, bus o horarios
                if (request.busId() != null || request.localDate() != null ||
                    request.departureTime() != null || request.arrivalTime() != null) {

                    List<Trip> conflicts = tripRepository.findConflictingTrips(
                            trip.getBus().getId(),
                            trip.getDate(),
                            trip.getDepartureTime(),
                            trip.getArrivalTime()
                    );

                    // Excluir el viaje actual de los conflictos
                    conflicts.removeIf(t -> t.getId().equals(trip.getId()));

                    if (!conflicts.isEmpty()) {
                        throw new IllegalStateException(
                                "Bus %s has conflicting trips after update on %s"
                                        .formatted(trip.getBus().getPlate(), trip.getDate())
                        );
                    }
                }

                log.info("Updating trip {}: route={}, bus={}, date={}",
                        id, trip.getRoute().getId(), trip.getBus().getPlate(), trip.getDate());

                return mapper.toTripResponse(tripRepository.save(trip));
            }


            // ========================= GET =========================
            @Override
            public TripResponse get(Long id) {
                return tripRepository.findById(id)
                        .map(mapper::toTripResponse)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));
            }


            // ========================= DELETE =========================
            @Override
            @Transactional
            public void delete(Long id) {
                Trip trip = tripRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));

                if (!canBeDeleted(id)) {
                    throw new IllegalStateException(
                            "Cannot delete trip %d: it has sold tickets".formatted(id)
                    );
                }

                log.warn("Deleting trip {}: route={}, bus={}, date={}",
                        id, trip.getRoute().getId(), trip.getBus().getPlate(), trip.getDate());

                tripRepository.delete(trip);
            }


            // ========================= QUERIES =========================
            @Override
            public List<TripResponse> findByStatusAndBusId(TripStatus status, Long busId) {
                List<Trip> trips = tripRepository.findByStatusAndBusId(status, busId);

                if (trips.isEmpty()) {
                    throw new NotFoundException(
                            "No trips found with status %s and bus %d".formatted(status, busId)
                    );
                }

                return trips.stream().map(mapper::toTripResponse).toList();
            }

            @Override
            public List<TripResponse> findByRouteId(Long routeId) {
                List<Trip> trips = tripRepository.findByRouteId(routeId);

                if (trips.isEmpty()) {
                    throw new NotFoundException("No trips found for route %d".formatted(routeId));
                }

                return trips.stream().map(mapper::toTripResponse).toList();
            }

            @Override
            public List<TripResponse> findByBusId(Long busId) {
                List<Trip> trips = tripRepository.findByBusId(busId);

                if (trips.isEmpty()) {
                    throw new NotFoundException("No trips found for bus %d".formatted(busId));
                }

                return trips.stream().map(mapper::toTripResponse).toList();
            }

            @Override
            public List<TripResponse> findByStatus(TripStatus status) {
                List<Trip> trips = tripRepository.findByStatus(status);

                if (trips.isEmpty()) {
                    throw new NotFoundException("No trips found with status %s".formatted(status));
                }

                return trips.stream().map(mapper::toTripResponse).toList();
            }


            // ========================= BÚSQUEDA AVANZADA =========================
            @Override
            public List<TripResponse> searchAvailableTrips(Long routeId, LocalDate date) {
                List<Trip> trips = tripRepository.findAvailableTrips(routeId, date, OffsetDateTime.now());

                if (trips.isEmpty()) {
                    throw new NotFoundException(
                            "No available trips found for route %d on %s".formatted(routeId, date)
                    );
                }

                return trips.stream().map(mapper::toTripResponse).toList();
            }

            @Override
            public List<TripResponse> searchTrips(Long routeId, LocalDate date, TripStatus status) {
                List<Trip> trips;

                if (status != null) {
                    trips = tripRepository.findByRouteIdAndDateAndStatus(routeId, date, status);
                } else {
                    trips = tripRepository.findByRouteIdAndDate(routeId, date);
                }

                if (trips.isEmpty()) {
                    throw new NotFoundException(
                            "No trips found for route %d on %s with status %s"
                                    .formatted(routeId, date, status)
                    );
                }

                return trips.stream().map(mapper::toTripResponse).toList();
            }


            // ========================= GESTIÓN DE ESTADOS =========================
            @Override
            @Transactional
            public TripResponse boardTrip(Long tripId) {
                Trip trip = tripRepository.findById(tripId)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

                if (trip.getStatus() != TripStatus.SCHEDULED) {
                    throw new IllegalStateException(
                            "Only SCHEDULED trips can start boarding. Current status: %s"
                                    .formatted(trip.getStatus())
                    );
                }

                log.info("Starting boarding for trip {}: route={}, bus={}",
                        tripId, trip.getRoute().getId(), trip.getBus().getPlate());

                trip.setStatus(TripStatus.BOARDING);
                return mapper.toTripResponse(tripRepository.save(trip));
            }

            @Override
            @Transactional
            public TripResponse departTrip(Long tripId) {
                var trip = tripRepository.findById(tripId)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

                if (trip.getStatus() != TripStatus.BOARDING) {
                    throw new IllegalStateException(
                            "Only BOARDING trips can depart. Current status: %s"
                                    .formatted(trip.getStatus())
                    );
                }

                // Validate driver assignment with approved checklist
                var assignment = assignmentRepository.findFirstByTripId(tripId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Trip %d must have a driver assigned before departure".formatted(tripId)
                        ));

                if (!assignment.isCheckListOk()) {
                    throw new IllegalStateException(
                            "Trip %d cannot depart: checklist must be completed and approved by dispatcher"
                                    .formatted(tripId)
                    );
                }

                log.info("Trip {} departing: route={}, bus={}, driver={}, checklist=OK",
                        tripId, trip.getRoute().getId(), trip.getBus().getPlate(),
                        assignment.getDriver().getId());

                trip.setStatus(TripStatus.DEPARTED);
                return mapper.toTripResponse(tripRepository.save(trip));
            }

            @Override
            @Transactional
            public TripResponse arriveTrip(Long tripId) {
                Trip trip = tripRepository.findById(tripId)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

                if (trip.getStatus() != TripStatus.DEPARTED) {
                    throw new IllegalStateException(
                            "Only DEPARTED trips can arrive. Current status: %s"
                                    .formatted(trip.getStatus())
                    );
                }

                log.info("Trip {} arrived: route={}, bus={}",
                        tripId, trip.getRoute().getId(), trip.getBus().getPlate());

                trip.setStatus(TripStatus.ARRIVED);
                return mapper.toTripResponse(tripRepository.save(trip));
            }

            @Override
            @Transactional
            public TripResponse cancelTrip(Long tripId) {
                Trip trip = tripRepository.findById(tripId)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

                if (trip.getStatus() == TripStatus.ARRIVED) {
                    throw new IllegalStateException("Cannot cancel an ARRIVED trip");
                }

                if (trip.getStatus() == TripStatus.CANCELLED) {
                    throw new IllegalStateException("Trip is already CANCELLED");
                }

                if (trip.getStatus() == TripStatus.DEPARTED) {
                    throw new IllegalStateException("Cannot cancel a trip that has already departed");
                }

                if (hasTicketsSold(tripId)) {
                    log.error("Attempted to cancel trip {} with sold tickets", tripId);
                    throw new IllegalStateException(
                            "Cannot cancel trip %d: it has sold tickets. Please refund them first."
                                    .formatted(tripId)
                    );
                }

                log.warn("Cancelling trip {}: route={}, bus={}, date={}",
                        tripId, trip.getRoute().getId(), trip.getBus().getPlate(), trip.getDate());

                trip.setStatus(TripStatus.CANCELLED);
                return mapper.toTripResponse(tripRepository.save(trip));
            }

            @Override
            @Transactional
            public TripResponse rescheduleTrip(Long tripId) {
                Trip trip = tripRepository.findById(tripId)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

                if (trip.getStatus() != TripStatus.CANCELLED) {
                    throw new IllegalStateException(
                            "Only CANCELLED trips can be rescheduled. Current status: %s"
                                    .formatted(trip.getStatus())
                    );
                }

                log.info("Rescheduling trip {}: route={}, bus={}, date={}",
                        tripId, trip.getRoute().getId(), trip.getBus().getPlate(), trip.getDate());

                trip.setStatus(TripStatus.SCHEDULED);
                return mapper.toTripResponse(tripRepository.save(trip));
            }


            // ========================= DISPONIBILIDAD =========================
            @Override
            public Integer getAvailableSeatsCount(Long tripId) {
                Trip trip = tripRepository.findById(tripId)
                        .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

                Integer totalSeats = trip.getBus().getCapacity();
                Integer occupiedSeats = getOccupiedSeatsCount(tripId);

                return totalSeats - occupiedSeats;
            }

            @Override
            public Integer getOccupiedSeatsCount(Long tripId) {
                List<Ticket> soldTickets = ticketRepository.findByTripId(tripId).stream()
                        .filter(ticket -> ticket.getStatus() == TicketStatus.SOLD)
                        .toList();

                Set<String> uniqueSeats = new HashSet<>();
                for (Ticket ticket : soldTickets) {
                    uniqueSeats.add(ticket.getSeatNumber());
                }

                return uniqueSeats.size();
            }

            @Override
            public boolean isBusAvailable(Long busId, LocalDate date) {
                List<Trip> activeTrips = tripRepository.findActiveTripsByBusAndDate(busId, date);
                return activeTrips.isEmpty();
            }


            // ========================= VALIDACIONES =========================
            @Override
            public boolean canBeDeleted(Long tripId) {
                return !hasTicketsSold(tripId);
            }

            @Override
            public boolean hasTicketsSold(Long tripId) {
                List<Ticket> soldTickets = ticketRepository.findByTripId(tripId).stream()
                        .filter(ticket -> ticket.getStatus() == TicketStatus.SOLD)
                        .toList();

                return !soldTickets.isEmpty();
            }
        }