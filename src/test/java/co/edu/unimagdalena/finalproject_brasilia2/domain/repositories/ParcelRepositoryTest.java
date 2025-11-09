package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
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

class ParcelRepositoryTest extends AbstractRepositoryIT {
    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private TripRepository tripRepository;

    private Stop stopBarranquilla;
    private Stop stopCienaga;
    private Stop stopSantaMarta;
    private Route route;
    private Bus bus;
    private Trip trip;
    private Parcel parcel1;
    private Parcel parcel2;
    private Parcel parcel3;
    private Parcel parcel4;
    private Parcel parcel5;
    private OffsetDateTime baseTime;
    @BeforeEach
    void setUp() {
        parcelRepository.deleteAll();
        tripRepository.deleteAll();
        stopRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();

        baseTime = OffsetDateTime.now();

        // Crear bus
        bus = Bus.builder()
                .plate("BUS123")
                .capacity(40)
                .status(true)
                .build();
        busRepository.save(bus);

        // Crear ruta
        route = Route.builder()
                .code("RUT001")
                .name("Ruta Costa Caribe")
                .origin("Barranquilla")
                .destination("Santa Marta")
                .distanceKm(new BigDecimal("95.50"))
                .durationMin(90)
                .build();
        routeRepository.save(route);

        // Crear paradas
        stopBarranquilla = Stop.builder()
                .route(route)
                .name("Terminal Barranquilla")
                .order(1)
                .lat(10.9685)
                .lng(-74.7813)
                .build();

        stopCienaga = Stop.builder()
                .route(route)
                .name("Terminal Ciénaga")
                .order(2)
                .lat(11.0054)
                .lng(-74.2470)
                .build();

        stopSantaMarta = Stop.builder()
                .route(route)
                .name("Terminal Santa Marta")
                .order(3)
                .lat(11.2408)
                .lng(-74.2050)
                .build();

        stopRepository.save(stopBarranquilla);
        stopRepository.save(stopCienaga);
        stopRepository.save(stopSantaMarta);

        // Crear viaje (si tu entidad Parcel necesita trip)
        trip = Trip.builder()
                .route(route)
                .bus(bus)
                .date(LocalDate.now().plusDays(1))
                .departureTime(baseTime.plusDays(1))
                .arrivalTime(baseTime.plusDays(1).plusHours(2))
                .status(TripStatus.SCHEDULED)
                .build();
        tripRepository.save(trip);

        // Crear paquetes
        parcel1 = Parcel.builder()
                .code("PKG001")
                .senderName("Juan Pérez")
                .senderPhone("3001234567")
                .receiverName("María García")
                .receiverPhone("3007654321")
                .fromStop(stopBarranquilla)
                .toStop(stopSantaMarta)
                .price(new BigDecimal("25000"))
                .status(ParcelStatus.IN_TRANSIT)
                .deliveryOtp("12345678")
                .build();

        parcel2 = Parcel.builder()
                .code("PKG002")
                .senderName("Juan Pérez")
                .senderPhone("3001234567")
                .receiverName("Carlos López")
                .receiverPhone("3009876543")
                .fromStop(stopBarranquilla)
                .toStop(stopCienaga)
                .price(new BigDecimal("15000"))
                .status(ParcelStatus.IN_TRANSIT)
                .deliveryOtp("87654321")
                .build();

        parcel3 = Parcel.builder()
                .code("PKG003")
                .senderName("Ana Martínez")
                .senderPhone("3005556666")
                .receiverName("Pedro González")
                .receiverPhone("3002223333")
                .fromStop(stopCienaga)
                .toStop(stopSantaMarta)
                .price(new BigDecimal("10000"))
                .status(ParcelStatus.DELIVERED)
                .deliveryOtp("11112222")
                .build();

        parcel4 = Parcel.builder()
                .code("PKG004")
                .senderName("María García")
                .senderPhone("3007654321")
                .receiverName("Juan Pérez")
                .receiverPhone("3001234567")
                .fromStop(stopBarranquilla)
                .toStop(stopSantaMarta)
                .price(new BigDecimal("30000"))
                .status(ParcelStatus.FAILED)
                .deliveryOtp("99998888")
                .build();

        parcel5 = Parcel.builder()
                .code("PKG005")
                .senderName("Carlos López")
                .senderPhone("3009876543")
                .receiverName("María García")
                .receiverPhone("3007654321")
                .fromStop(stopSantaMarta)
                .toStop(stopBarranquilla)
                .price(new BigDecimal("20000"))
                .status(ParcelStatus.FAILED)
                .deliveryOtp("55554444")
                .build();
    }

    @Test
    @DisplayName("Parcel: find by sender name")
    void shouldFindBySenderName() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);

        // When
        var juanParcels = parcelRepository.findBySenderName("Juan Pérez");
        var anaParcels = parcelRepository.findBySenderName("Ana Martínez");

        // Then
        assertThat(juanParcels).hasSize(2);
        assertThat(juanParcels)
                .extracting(Parcel::getCode)
                .containsExactlyInAnyOrder("PKG001", "PKG002");

        assertThat(anaParcels).hasSize(1);
        assertThat(anaParcels.get(0).getCode()).isEqualTo("PKG003");
    }
    @Test
    @DisplayName("Parcel: find by sender phone")
    void shouldFindBySenderPhone() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);

        // When
        var result = parcelRepository.findBySenderPhone("3001234567");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Parcel::getSenderName)
                .allMatch(name -> name.equals("Juan Pérez"));
        assertThat(result)
                .extracting(Parcel::getReceiverName)
                .containsExactlyInAnyOrder("María García", "Carlos López");
    }

    @Test
    @DisplayName("Parcel: find by receiver name")
    void shouldFindByReceiverName() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel4);
        parcelRepository.save(parcel5);

        // When
        var mariaReceives = parcelRepository.findByReceiverName("María García");
        var juanReceives = parcelRepository.findByReceiverName("Juan Pérez");

        // Then
        assertThat(mariaReceives).hasSize(2);
        assertThat(mariaReceives)
                .extracting(Parcel::getCode)
                .containsExactlyInAnyOrder("PKG001", "PKG005");

        assertThat(juanReceives).hasSize(1);
        assertThat(juanReceives.get(0).getCode()).isEqualTo("PKG004");
    }

    @Test
    @DisplayName("Parcel: find by receiver phone")
    void shouldFindByReceiverPhone() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel4);
        parcelRepository.save(parcel5);

        // When
        var result = parcelRepository.findByReceiverPhone("3007654321");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Parcel::getCode)
                .containsExactlyInAnyOrder("PKG001", "PKG005");
        assertThat(result)
                .extracting(Parcel::getReceiverName)
                .allMatch(name -> name.equals("María García"));
    }

    @Test
    @DisplayName("Parcel: find by status")
    void shouldFindByStatus() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);
        parcelRepository.save(parcel4);
        parcelRepository.save(parcel5);

        // When
        var pendingParcels = parcelRepository.findByStatus(ParcelStatus.IN_TRANSIT);
        var inTransitParcels = parcelRepository.findByStatus(ParcelStatus.IN_TRANSIT);
        var deliveredParcels = parcelRepository.findByStatus(ParcelStatus.DELIVERED);
        var cancelledParcels = parcelRepository.findByStatus(ParcelStatus.FAILED);
        var failedParcels = parcelRepository.findByStatus(ParcelStatus.FAILED);

        // Then
        assertThat(pendingParcels).hasSize(2);
        assertThat(pendingParcels.get(0).getCode()).isEqualTo("PKG001");

        assertThat(inTransitParcels).hasSize(2);
        assertThat(inTransitParcels.get(0).getCode()).isEqualTo("PKG001");

        assertThat(deliveredParcels).hasSize(1);
        assertThat(deliveredParcels.get(0).getCode()).isEqualTo("PKG003");

        assertThat(cancelledParcels).hasSize(2);
        assertThat(cancelledParcels.get(0).getCode()).isEqualTo("PKG004");

        assertThat(failedParcels).hasSize(2);
        assertThat(failedParcels.get(0).getCode()).isEqualTo("PKG004");
    }

    @Test
    @DisplayName("Parcel: find by from stop id")
    void shouldFindByFromStopId() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);
        parcelRepository.save(parcel4);
        parcelRepository.save(parcel5);

        // When
        var fromBarranquilla = parcelRepository.findByFromStopId(stopBarranquilla.getId());
        var fromCienaga = parcelRepository.findByFromStopId(stopCienaga.getId());
        var fromSantaMarta = parcelRepository.findByFromStopId(stopSantaMarta.getId());

        // Then
        assertThat(fromBarranquilla).hasSize(3);
        assertThat(fromBarranquilla)
                .extracting(Parcel::getCode)
                .containsExactlyInAnyOrder("PKG001", "PKG002", "PKG004");

        assertThat(fromCienaga).hasSize(1);
        assertThat(fromCienaga.get(0).getCode()).isEqualTo("PKG003");

        assertThat(fromSantaMarta).hasSize(1);
        assertThat(fromSantaMarta.get(0).getCode()).isEqualTo("PKG005");
    }

    @Test
    @DisplayName("Parcel: find by to stop id")
    void shouldFindByToStopId() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);
        parcelRepository.save(parcel4);
        parcelRepository.save(parcel5);

        // When
        var toBarranquilla = parcelRepository.findByToStopId(stopBarranquilla.getId());
        var toCienaga = parcelRepository.findByToStopId(stopCienaga.getId());
        var toSantaMarta = parcelRepository.findByToStopId(stopSantaMarta.getId());

        // Then
        assertThat(toBarranquilla).hasSize(1);
        assertThat(toBarranquilla.get(0).getCode()).isEqualTo("PKG005");

        assertThat(toCienaga).hasSize(1);
        assertThat(toCienaga.get(0).getCode()).isEqualTo("PKG002");

        assertThat(toSantaMarta).hasSize(3);
        assertThat(toSantaMarta)
                .extracting(Parcel::getCode)
                .containsExactlyInAnyOrder("PKG001", "PKG003", "PKG004");
    }

    @Test
    @DisplayName("Parcel: find by sender name case sensitive")
    void shouldFindBySenderNameCaseSensitive() {
        // Given
        parcelRepository.save(parcel1);

        // When
        var exactMatch = parcelRepository.findBySenderName("Juan Pérez");
        var wrongCase = parcelRepository.findBySenderName("juan pérez");

        // Then
        assertThat(exactMatch).hasSize(1);
        assertThat(wrongCase).isEmpty(); // Spring Data es case-sensitive por defecto
    }
    @Test
    @DisplayName("Parcel: return empty list when sender not found")
    void shouldReturnEmptyWhenSenderNotFound() {
        // Given
        parcelRepository.save(parcel1);

        // When
        var result = parcelRepository.findBySenderName("Usuario Inexistente");

        // Then
        assertThat(result).isEmpty();
    }
    @Test
    @DisplayName("Parcel: return empty list when receiver not found")
    void shouldReturnEmptyWhenReceiverNotFound() {
        // Given
        parcelRepository.save(parcel1);

        // When
        var result = parcelRepository.findByReceiverPhone("9999999999");

        // Then
        assertThat(result).isEmpty();
    }
}