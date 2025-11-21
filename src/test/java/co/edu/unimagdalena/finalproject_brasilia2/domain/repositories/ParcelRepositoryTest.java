package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Parcel;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ParcelRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Route route;
    private Stop stop1;
    private Stop stop2;
    private Stop stop3;
    private Parcel parcel1;
    private Parcel parcel2;
    private Parcel parcel3;
    private Parcel parcel4;

    @BeforeEach
    void setUp() {
        parcelRepository.deleteAll();
        stopRepository.deleteAll();
        routeRepository.deleteAll();

        // Create route
        route = Route.builder()
                .code("R001")
                .name("Barranquilla-Santa Marta")
                .origin("Barranquilla")
                .destination("Santa Marta")
                .distanceKm(new BigDecimal("100"))
                .durationMin(120)
                .build();
        route = routeRepository.save(route);

        // Create stops
        stop1 = Stop.builder()
                .route(route)
                .name("Terminal Barranquilla")
                .order(1)
                .lat(10.9685)
                .lng(-74.7813)
                .build();
        stop1 = stopRepository.save(stop1);

        stop2 = Stop.builder()
                .route(route)
                .name("Terminal Cienaga")
                .order(2)
                .lat(11.0061)
                .lng(-74.2466)
                .build();
        stop2 = stopRepository.save(stop2);

        stop3 = Stop.builder()
                .route(route)
                .name("Terminal Santa Marta")
                .order(3)
                .lat(11.2408)
                .lng(-74.2099)
                .build();
        stop3 = stopRepository.save(stop3);

        // Create parcels
        parcel1 = Parcel.builder()
                .code("PCL001")
                .senderName("Juan Rodriguez")
                .senderPhone("3001234567")
                .receiverName("Maria Gomez")
                .receiverPhone("3109876543")
                .fromStop(stop1)
                .toStop(stop2)
                .price(new BigDecimal("15000"))
                .status(ParcelStatus.CREATED)
                .deliveryOtp("12345678")
                .build();

        parcel2 = Parcel.builder()
                .code("PCL002")
                .senderName("Juan Rodriguez")
                .senderPhone("3001234567")
                .receiverName("Carlos Perez")
                .receiverPhone("3201122334")
                .fromStop(stop1)
                .toStop(stop3)
                .price(new BigDecimal("20000"))
                .status(ParcelStatus.IN_TRANSIT)
                .deliveryOtp("87654321")
                .build();

        parcel3 = Parcel.builder()
                .code("PCL003")
                .senderName("Ana Lopez")
                .senderPhone("3154445566")
                .receiverName("Pedro Santos")
                .receiverPhone("3007778899")
                .fromStop(stop2)
                .toStop(stop3)
                .price(new BigDecimal("12000"))
                .status(ParcelStatus.DELIVERED)
                .deliveryOtp("11223344")
                .build();

        parcel4 = Parcel.builder()
                .code("PCL004")
                .senderName("Luis Martinez")
                .senderPhone("3189990000")
                .receiverName("Sofia Ramirez")
                .receiverPhone("3156667777")
                .fromStop(stop1)
                .toStop(stop2)
                .price(new BigDecimal("18000"))
                .status(ParcelStatus.FAILED)
                .deliveryOtp("99887766")
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
        var result = parcelRepository.findBySenderName("Juan Rodriguez");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Parcel::getCode)
                .containsExactlyInAnyOrder("PCL001", "PCL002");
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
                .extracting(Parcel::getReceiverName)
                .containsExactlyInAnyOrder("Maria Gomez", "Carlos Perez");
    }

    @Test
    @DisplayName("Parcel: find by receiver name")
    void shouldFindByReceiverName() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);

        // When
        var result = parcelRepository.findByReceiverName("Maria Gomez");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PCL001");
        assertThat(result.get(0).getSenderName()).isEqualTo("Juan Rodriguez");
    }

    @Test
    @DisplayName("Parcel: find by receiver phone")
    void shouldFindByReceiverPhone() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);

        // When
        var result = parcelRepository.findByReceiverPhone("3007778899");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PCL003");
        assertThat(result.get(0).getReceiverName()).isEqualTo("Pedro Santos");
    }

    @Test
    @DisplayName("Parcel: find by status")
    void shouldFindByStatus() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);
        parcelRepository.save(parcel4);

        // When
        var inTransitParcels = parcelRepository.findByStatus(ParcelStatus.IN_TRANSIT);
        var deliveredParcels = parcelRepository.findByStatus(ParcelStatus.DELIVERED);

        // Then
        assertThat(inTransitParcels).hasSize(1);
        assertThat(inTransitParcels.get(0).getCode()).isEqualTo("PCL002");

        assertThat(deliveredParcels).hasSize(1);
        assertThat(deliveredParcels.get(0).getCode()).isEqualTo("PCL003");
    }

    @Test
    @DisplayName("Parcel: find by from stop id")
    void shouldFindByFromStopId() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);

        // When
        var result = parcelRepository.findByFromStopId(stop1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Parcel::getCode)
                .containsExactlyInAnyOrder("PCL001", "PCL002");
    }

    @Test
    @DisplayName("Parcel: find by to stop id")
    void shouldFindByToStopId() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);

        // When
        var result = parcelRepository.findByToStopId(stop3.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Parcel::getCode)
                .containsExactlyInAnyOrder("PCL002", "PCL003");
    }

    @Test
    @DisplayName("Parcel: find by code")
    void shouldFindByCode() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);

        // When
        var result = parcelRepository.findByCode("PCL001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSenderName()).isEqualTo("Juan Rodriguez");
        assertThat(result.get().getReceiverName()).isEqualTo("Maria Gomez");
        assertThat(result.get().getDeliveryOtp()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Parcel: find by to stop id and status")
    void shouldFindByToStopIdAndStatus() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);

        // When
        var result = parcelRepository.findByToStopIdAndStatus(
                stop3.getId(),
                ParcelStatus.DELIVERED
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PCL003");
    }

    @Test
    @DisplayName("Parcel: count by status")
    void shouldCountByStatus() {
        // Given
        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);
        parcelRepository.save(parcel3);
        parcelRepository.save(parcel4);

        // When
        var createdCount = parcelRepository.countByStatus(ParcelStatus.CREATED);
        var inTransitCount = parcelRepository.countByStatus(ParcelStatus.IN_TRANSIT);
        var deliveredCount = parcelRepository.countByStatus(ParcelStatus.DELIVERED);
        var failedCount = parcelRepository.countByStatus(ParcelStatus.FAILED);

        // Then
        assertThat(createdCount).isEqualTo(1L);
        assertThat(inTransitCount).isEqualTo(1L);
        assertThat(deliveredCount).isEqualTo(1L);
        assertThat(failedCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("Parcel: check if exists by code")
    void shouldCheckExistsByCode() {
        // Given
        parcelRepository.save(parcel1);

        // When
        var exists = parcelRepository.existsByCode("PCL001");
        var notExists = parcelRepository.existsByCode("PCL999");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Parcel: return empty when code not found")
    void shouldReturnEmptyWhenCodeNotFound() {
        // Given
        parcelRepository.save(parcel1);

        // When
        var result = parcelRepository.findByCode("PCL999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Parcel: return empty list when sender has no parcels")
    void shouldReturnEmptyWhenSenderHasNoParcels() {
        // Given
        parcelRepository.save(parcel1);

        // When
        var result = parcelRepository.findBySenderName("Unknown Sender");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Parcel: return empty list when status has no parcels")
    void shouldReturnEmptyWhenStatusHasNoParcels() {
        // Given
        parcelRepository.save(parcel1);

        // When
        var result = parcelRepository.findByStatus(ParcelStatus.DELIVERED);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Parcel: return zero count when status has no parcels")
    void shouldReturnZeroCountWhenStatusHasNoParcels() {
        // Given - no parcels saved

        // When
        var count = parcelRepository.countByStatus(ParcelStatus.CREATED);

        // Then
        assertThat(count).isEqualTo(0L);
    }
}