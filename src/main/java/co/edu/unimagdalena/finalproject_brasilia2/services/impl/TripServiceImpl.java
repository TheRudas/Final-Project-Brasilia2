package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.TripService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.TripMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final TripMapper mapper;


    // ====================== CREATE ======================

    @Override
    @Transactional
    public TripResponse create(TripCreateRequest request) {

        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(request.routeId())));

        Bus bus = busRepository.findById(request.busId())
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(request.busId())));

        Trip trip = mapper.toEntity(request);

        trip.setRoute(route);
        trip.setBus(bus);

        trip.setStatus(TripStatus.DEPARTED);

        trip.setDate(LocalDate.parse(request.localDate()));
        trip.setDepartureTime(OffsetDateTime.parse(request.departureTime()));
        trip.setArrivalTime(OffsetDateTime.parse(request.arrivalTime()));

        return mapper.toTripResponse(tripRepository.save(trip));
    }


    // ====================== UPDATE ======================

    @Override
    @Transactional
    public TripResponse update(Long id, TripUpdateRequest request) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));

        // Apply partial updates using mapper
        mapper.patch(trip, request);

        // Route update if present
        if (request.routeId() != null) {
            Route route = routeRepository.findById(request.routeId())
                    .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(request.routeId())));
            trip.setRoute(route);
        }

        // Bus update if present
        if (request.busId() != null) {
            Bus bus = busRepository.findById(request.busId())
                    .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(request.busId())));
            trip.setBus(bus);
        }

        // Dates
        if (request.localDate() != null) {
            trip.setDate(LocalDate.parse(request.localDate()));
        }

        if (request.departureTime() != null) {
            trip.setDepartureTime(OffsetDateTime.parse(request.departureTime()));
        }

        if (request.arrivalTime() != null) {
            trip.setArrivalTime(OffsetDateTime.parse(request.arrivalTime()));
        }

        return mapper.toTripResponse(tripRepository.save(trip));
    }


    // ====================== GET ======================

    @Override
    public TripResponse get(Long id) {
        return tripRepository.findById(id)
                .map(mapper::toTripResponse)
                .orElseThrow(() ->
                        new NotFoundException("Trip %d not found".formatted(id)));
    }


    // ====================== DELETE ======================

    @Override
    @Transactional
    public void delete(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Trip %d not found".formatted(id)));

        tripRepository.delete(trip);
    }


    // ====================== QUERY ======================

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
}
