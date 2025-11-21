package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.ParcelServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.ParcelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Parcel Service Tests")
class ParcelServiceImplTest {

    @Mock
    private ParcelRepository repository;
    
    @Mock
    private StopRepository stopRepository;
    
    @Mock
    private TripRepository tripRepository;
    
    @Mock
    private IncidentRepository incidentRepository;
    
    @Mock
    private ConfigService configService;
    
    @Mock
    private NotificationService notificationService;
    
    @Spy
    private ParcelMapper mapper = Mappers.getMapper(ParcelMapper.class);
    
    @InjectMocks
    private ParcelServiceImpl parcelService;
    
    private Route testRoute;
    private Stop fromStop;
    private Stop toStop;
    private Parcel testParcel;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        testRoute = Route.builder()
                .id(1L)
                .name("Bogota - Medellin")
                .code("BOG-MED-001")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400"))
                .durationMin(360)
                .build();

        fromStop = Stop.builder()
                .id(1L)
                .name("Terminal Bogota")
                .route(testRoute)
                .order(1)
                .build();

        toStop = Stop.builder()
                .id(2L)
                .name("Terminal Medellin")
                .route(testRoute)
                .order(5)
                .build();

        testParcel = Parcel.builder()
                .id(1L)
                .code("PARCEL-001")
                .senderName("Alice Sender")
                .senderPhone("1111111111")
                .receiverName("Bob Receiver")
                .receiverPhone("2222222222")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("25000"))
                .status(ParcelStatus.CREATED)
                .deliveryOtp("12345678")
                .build();

        Bus testBus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .status(true)
                .build();

        testTrip = Trip.builder()
                .id(1L)
                .route(testRoute)
                .bus(testBus)
                .date(LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusHours(8))
                .status(TripStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("Should create parcel successfully and send confirmation notification")
    void shouldCreateParcelSuccessfully() {
        ParcelCreateRequest request = new ParcelCreateRequest(
                "PARCEL-001",
                "Alice Sender",
                "1111111111",
                "Bob Receiver",
                "2222222222",
                1L,
                2L
        );

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(configService.getValue("PARCEL_BASE_PRICE_PER_KM")).thenReturn(new BigDecimal("50"));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> {
            Parcel p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ParcelResponse response = parcelService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.senderName()).isEqualTo("Alice Sender");
        verify(notificationService).sendParcelConfirmation(
                eq("1111111111"), eq("Alice Sender"), anyString(), eq("Bob Receiver"), eq("2222222222")
        );
    }

    @Test
    @DisplayName("Should assign parcel to trip and send in-transit notification")
    void shouldAssignParcelToTripSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(testParcel));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        parcelService.assignToTrip(1L, 1L);

        verify(notificationService).sendParcelInTransit(
                eq("2222222222"), eq("Bob Receiver"), eq("PARCEL-001")
        );
    }

    @Test
    @DisplayName("Should deliver parcel with valid OTP and send notification")
    void shouldDeliverParcelWithValidOTP() {
        testParcel.setStatus(ParcelStatus.IN_TRANSIT);
        when(repository.findById(1L)).thenReturn(Optional.of(testParcel));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        parcelService.deliverParcel(1L, "12345678");

        verify(notificationService).sendParcelDelivered(
                eq("2222222222"), eq("Bob Receiver"), eq("PARCEL-001")
        );
    }

    @Test
    @DisplayName("Should fail delivery with invalid OTP and send failure notification")
    void shouldFailDeliveryWithInvalidOTP() {
        testParcel.setStatus(ParcelStatus.IN_TRANSIT);
        when(repository.findById(1L)).thenReturn(Optional.of(testParcel));
        when(repository.save(any(Parcel.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> parcelService.deliverParcel(1L, "wrongOTP"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(notificationService).sendParcelDeliveryFailed(
                eq("2222222222"), eq("Bob Receiver"), eq("PARCEL-001"), eq(1L), eq("Invalid OTP provided")
        );
    }
}

