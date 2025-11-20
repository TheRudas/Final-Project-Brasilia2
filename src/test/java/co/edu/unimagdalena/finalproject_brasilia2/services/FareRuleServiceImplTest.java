package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PassengerType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.*;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.FareRuleServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.FareRuleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareRuleServiceImplTest {

    @Mock
    private FareRuleRepository fareRuleRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private StopRepository stopRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private ConfigService configService;
    @Mock
    private TripService tripService;

    @Spy
    private FareRuleMapper mapper = Mappers.getMapper(FareRuleMapper.class);

    @InjectMocks
    private FareRuleServiceImpl service;

    // ============= HELPER METHODS =============

    private Route createTestRoute() {
        return Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Bogota-Medellin")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.00"))
                .durationMin(360)
                .build();
    }

    private Stop createTestStop(Route route, String name, Integer order) {
        return Stop.builder()
                .id(order.longValue())
                .route(route)
                .name(name)
                .order(order)
                .lat(4.7110)
                .lng(-74.0721)
                .build();
    }

    private Bus createTestBus() {
        return Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashSet<>())
                .status(true)
                .build();
    }

    private Trip createTestTrip(Route route, Bus bus) {
        return Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .date(java.time.LocalDate.now())
                .departureTime(OffsetDateTime.now().plusHours(2))
                .arrivalTime(OffsetDateTime.now().plusHours(8))
                .status(co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus.SCHEDULED)
                .build();
    }

    private FareRule createTestFareRule(Route route, Stop fromStop, Stop toStop) {
        return FareRule.builder()
                .id(1L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000.00"))
                .discounts(new HashSet<>())
                .dynamicPricing(false)
                .build();
    }

    // ============= CREATE TESTS =============

    @Test
    void shouldCreateFareRuleSuccessfully() {
        // Given
        var route = createTestRoute();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var request = new FareRuleCreateRequest(1L, 1L, 5L, new BigDecimal("50000.00"));

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.existsByRouteIdAndFromStopIdAndToStopId(1L, 1L, 5L)).thenReturn(false);
        when(fareRuleRepository.save(any(FareRule.class))).thenAnswer(invocation -> {
            FareRule saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        var result = service.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.routeId()).isEqualTo(1L);
        assertThat(result.fromStopId()).isEqualTo(1L);
        assertThat(result.toStopId()).isEqualTo(5L);
        assertThat(result.basePrice()).isEqualByComparingTo(new BigDecimal("50000.00"));

        verify(fareRuleRepository).existsByRouteIdAndFromStopIdAndToStopId(1L, 1L, 5L);
        verify(fareRuleRepository).save(any(FareRule.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateFareRule() {
        // Given
        var route = createTestRoute();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var request = new FareRuleCreateRequest(1L, 1L, 5L, new BigDecimal("50000.00"));

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.existsByRouteIdAndFromStopIdAndToStopId(1L, 1L, 5L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FareRule already exists");

        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRouteNotFound() {
        // Given
        var request = new FareRuleCreateRequest(
                999L, 1L, 5L,
                new BigDecimal("50000.00")
        );

        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 999 not found");
    }

    // ============= CALCULATE TICKET PRICE TESTS =============

    @Test
    void shouldCalculateTicketPriceWithExactFareRule() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);
        var fareRule = createTestFareRule(route, fromStop, toStop);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.findByRouteId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(fareRule)));
        when(configService.getValue("FARE_MINIMUM_PRICE")).thenReturn(new BigDecimal("5000.00"));

        // When
        var result = service.calculateTicketPrice(1L, 1L, 5L, PassengerType.ADULT);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(new BigDecimal("50000.00"));

        verify(tripRepository).findById(1L);
        verify(fareRuleRepository).findByRouteId(eq(1L), any(Pageable.class));
    }

    @Test
    void shouldCalculateTicketPriceWithDynamicPricing() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var fareRule = FareRule.builder()
                .id(1L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000.00"))
                .discounts(new HashSet<>())
                .dynamicPricing(true) // Habilitado
                .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.findByRouteId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(fareRule)));
        when(tripService.getOccupiedSeatsCount(1L)).thenReturn(36); // 90% ocupación
        when(configService.getValue("FARE_MINIMUM_PRICE")).thenReturn(new BigDecimal("5000.00"));

        // When
        var result = service.calculateTicketPrice(1L, 1L, 5L, PassengerType.ADULT);

        // Then
        // 50000 * 1.30 (90% ocupación) = 65000
        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(new BigDecimal("65000.00"));

        verify(tripService).getOccupiedSeatsCount(1L);
    }

    @Test
    void shouldCalculateTicketPriceWithChildDiscount() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);
        var fareRule = createTestFareRule(route, fromStop, toStop);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.findByRouteId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(fareRule)));
        when(configService.getValue("DISCOUNT_CHILD_PERCENT")).thenReturn(new BigDecimal("50")); // 50%
        when(configService.getValue("FARE_MINIMUM_PRICE")).thenReturn(new BigDecimal("5000.00"));

        // When
        var result = service.calculateTicketPrice(1L, 1L, 5L, PassengerType.CHILD);

        // Then
        // 50000 - 50% = 25000
        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(new BigDecimal("25000.00"));

        verify(configService).getValue("DISCOUNT_CHILD_PERCENT");
    }

    @Test
    void shouldCalculateTicketPriceWithStudentDiscount() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);
        var fareRule = createTestFareRule(route, fromStop, toStop);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.findByRouteId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(fareRule)));
        when(configService.getValue("DISCOUNT_STUDENT_PERCENT")).thenReturn(new BigDecimal("15")); // 15%
        when(configService.getValue("FARE_MINIMUM_PRICE")).thenReturn(new BigDecimal("5000.00"));

        // When
        var result = service.calculateTicketPrice(1L, 1L, 5L, PassengerType.STUDENT);

        // Then
        // 50000 - 15% = 42500
        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(new BigDecimal("42500.00"));

        verify(configService).getValue("DISCOUNT_STUDENT_PERCENT");
    }

    @Test
    void shouldCalculateProportionalFareWhenNoExactRuleExists() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Parada Intermedia", 3);

        var stops = List.of(
                createTestStop(route, "Stop 1", 1),
                createTestStop(route, "Stop 2", 2),
                createTestStop(route, "Stop 3", 3),
                createTestStop(route, "Stop 4", 4),
                createTestStop(route, "Stop 5", 5)
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(3L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.findByRouteId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of())); // No hay regla exacta
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(stops);
        when(configService.getValue("FARE_PRICE_PER_KM")).thenReturn(new BigDecimal("150")); // $150/km
        when(configService.getValue("FARE_MINIMUM_PRICE")).thenReturn(new BigDecimal("5000.00"));

        // When
        var result = service.calculateTicketPrice(1L, 1L, 3L, PassengerType.ADULT);

        // Then
        // 400km / 4 segmentos = 100km por segmento
        // 2 stops de distancia × 100km = 200km
        // 200km × 150 = 30000
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(new BigDecimal("5000.00")); // Mayor que mínimo

        verify(stopRepository).findByRouteIdOrderByOrderAsc(1L);
        verify(configService).getValue("FARE_PRICE_PER_KM");
    }

    @Test
    void shouldApplyMinimumPriceWhenCalculatedPriceIsLower() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);

        var fareRule = FareRule.builder()
                .id(1L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("3000.00")) // Precio bajo
                .discounts(new HashSet<>())
                .dynamicPricing(false)
                .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.findByRouteId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(fareRule)));
        when(configService.getValue("FARE_MINIMUM_PRICE")).thenReturn(new BigDecimal("5000.00"));

        // When
        var result = service.calculateTicketPrice(1L, 1L, 5L, PassengerType.ADULT);

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("5000.00")); // Precio mínimo aplicado
    }

    @Test
    void shouldThrowExceptionWhenTripNotFoundInCalculatePrice() {
        // Given
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.calculateTicketPrice(999L, 1L, 5L, PassengerType.ADULT))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 999 not found");
    }

    @Test
    void shouldThrowExceptionWhenFromStopNotBelongToRoute() {
        // Given
        var route1 = createTestRoute();
        var route2 = Route.builder()
                .id(2L)
                .code("RUT-002")
                .name("Otra Ruta")
                .build();

        var bus = createTestBus();
        var trip = createTestTrip(route1, bus);
        var fromStop = createTestStop(route2, "Stop de otra ruta", 1); // Ruta diferente
        var toStop = createTestStop(route1, "Stop correcto", 5);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));

        // When & Then
        assertThatThrownBy(() -> service.calculateTicketPrice(1L, 1L, 5L, PassengerType.ADULT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FromStop doesn't belong to trip's route");
    }

    @Test
    void shouldThrowExceptionWhenFromStopOrderIsGreaterOrEqualToToStop() {
        // Given
        var route = createTestRoute();
        var bus = createTestBus();
        var trip = createTestTrip(route, bus);
        var fromStop = createTestStop(route, "Terminal Medellin", 5);
        var toStop = createTestStop(route, "Terminal Bogota", 1); // Orden inverso

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(toStop));

        // When & Then
        assertThatThrownBy(() -> service.calculateTicketPrice(1L, 5L, 1L, PassengerType.ADULT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FromStop order must be less than ToStop order");
    }

    // ============= UPDATE & DELETE TESTS =============

    @Test
    void shouldUpdateFareRuleSuccessfully() {
        // Given
        var route = createTestRoute();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);
        var existingFareRule = createTestFareRule(route, fromStop, toStop);

        var updateRequest = new FareRuleUpdateRequest(
                1L,
                1L,
                5L,
                new BigDecimal("60000.00")
        );

        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(existingFareRule));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(5L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.save(any(FareRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var result = service.update(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.basePrice()).isEqualByComparingTo(new BigDecimal("60000.00"));

        verify(fareRuleRepository).save(any(FareRule.class));
    }

    @Test
    void shouldDeleteFareRuleSuccessfully() {
        // Given
        var route = createTestRoute();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);
        var fareRule = createTestFareRule(route, fromStop, toStop);

        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));

        // When
        service.delete(1L);

        // Then
        verify(fareRuleRepository).delete(fareRule);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentFareRule() {
        // Given
        when(fareRuleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("FareRule 999 not found");

        verify(fareRuleRepository, never()).delete(any());
    }

    // ============= GET TESTS =============

    @Test
    void shouldGetFareRuleById() {
        // Given
        var route = createTestRoute();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);
        var fareRule = createTestFareRule(route, fromStop, toStop);

        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));

        // When
        var result = service.get(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.routeId()).isEqualTo(1L);
    }

    @Test
    void shouldGetFareRulesByRouteId() {
        // Given
        var route = createTestRoute();
        var fromStop = createTestStop(route, "Terminal Bogota", 1);
        var toStop = createTestStop(route, "Terminal Medellin", 5);
        var fareRule = createTestFareRule(route, fromStop, toStop);

        var pageable = PageRequest.of(0, 10);
        when(fareRuleRepository.findByRouteId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(fareRule)));

        // When
        var result = service.getByRouteId(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().routeId()).isEqualTo(1L);
    }
}

