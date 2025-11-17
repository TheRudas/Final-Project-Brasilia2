package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TripRepositoryTest extends AbstractRepositoryIT{




    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Bus bus;
    private Route route;
    private Trip trip1;
    private Trip trip2;
    private Trip trip3;
    private OffsetDateTime baseTime;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        busRepository.deleteAll();
        routeRepository.deleteAll();

        baseTime = OffsetDateTime.now();

        bus = Bus.builder()
                .plate("XYZ123")
                .capacity(42)
                .status(true)
                .build();
        busRepository.save(bus);

        route = Route.builder()
                .code("RT01")
                .name("Ruta Centro")
                .origin("Santa Marta")
                .destination("Barranquilla")
                .distanceKm(new BigDecimal("94.5"))
                .durationMin(120)
                .build();
        routeRepository.save(route);

        trip1 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(baseTime.plusDays(1))
                .arrivalTime(baseTime.plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();

        trip2 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(2))
                .departureTime(baseTime.plusDays(2))
                .arrivalTime(baseTime.plusDays(2).plusHours(2))
                .status(TripStatus.BOARDING)
                .build();

        trip3 = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(3))
                .departureTime(baseTime.plusDays(3))
                .arrivalTime(baseTime.plusDays(3).plusHours(2))
                .status(TripStatus.CANCELLED)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);
    }

    @Test
    @DisplayName("Trip: find by ID")
    void findByID() {
        var result = tripRepository.findById(trip1.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Trip: find by status")
    void findByStatus() {
        var scheduledTrips = tripRepository.findByStatus(TripStatus.SCHEDULED);
        assertThat(scheduledTrips).hasSize(1);
    }

    @Test
    @DisplayName("Trip: find by bus ID")
    void findByBusId() {
        var trips = tripRepository.findByBusId(bus.getId());
        assertThat(trips).hasSize(3);
    }

    @Test
    @DisplayName("Trip: find by route ID")
    void findByRouteId() {
        var trips = tripRepository.findByRouteId(route.getId());
        assertThat(trips).hasSize(3);
    }

    @Test
    @DisplayName("Trip: find by status AND bus ID")
    void findByStatusAndBusId() {
        var activeTripsForBus = tripRepository.findByStatusAndBusId(TripStatus.BOARDING, bus.getId());
        assertThat(activeTripsForBus).hasSize(1);
        assertThat(activeTripsForBus.get(0).getStatus()).isEqualTo(TripStatus.BOARDING);
    }

    @Test
    @DisplayName("Trip: exists by route ID")
    void existsTripByRouteId() {
        var exists = tripRepository.existsTripByRouteId(route.getId());
        assertThat(exists).isTrue();
    }
}