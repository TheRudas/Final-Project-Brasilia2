package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripResponse;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;

import java.time.LocalDate;
import java.util.List;

public interface TripService {

    // ====================== CRUD BÁSICO ======================
    TripResponse create(TripCreateRequest request);
    TripResponse update(Long id, TripUpdateRequest request);
    TripResponse get(Long id);
    void delete(Long id);

    // ====================== QUERIES ======================
    List<TripResponse> findByStatusAndBusId(TripStatus status, Long busId);
    List<TripResponse> findByRouteId(Long routeId);
    List<TripResponse> findByBusId(Long busId);
    List<TripResponse> findByStatus(TripStatus status);

    // ====================== BÚSQUEDA AVANZADA ======================
    List<TripResponse> searchAvailableTrips(Long routeId, LocalDate date);
    List<TripResponse> searchTrips(Long routeId, LocalDate date, TripStatus status);

    // ====================== GESTIÓN DE ESTADOS ======================
    TripResponse boardTrip(Long tripId);        // SCHEDULED → BOARDING
    TripResponse departTrip(Long tripId);       // BOARDING → DEPARTED
    TripResponse arriveTrip(Long tripId);       // DEPARTED → ARRIVED
    TripResponse cancelTrip(Long tripId);       // Cualquier estado → CANCELLED
    TripResponse rescheduleTrip(Long tripId);   // CANCELLED → SCHEDULED

    // ====================== DISPONIBILIDAD ======================
    Integer getAvailableSeatsCount(Long tripId);
    Integer getOccupiedSeatsCount(Long tripId);
    boolean isBusAvailable(Long busId, LocalDate date);

    // ====================== VALIDACIONES ======================
    boolean canBeDeleted(Long tripId);
    boolean hasTicketsSold(Long tripId);
}