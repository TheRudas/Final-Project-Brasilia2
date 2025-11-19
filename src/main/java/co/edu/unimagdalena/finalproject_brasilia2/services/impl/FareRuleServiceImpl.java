package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.FareRuleRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.ConfigService;
import co.edu.unimagdalena.finalproject_brasilia2.services.FareRuleService;
import co.edu.unimagdalena.finalproject_brasilia2.services.TripService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.FareRuleMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository repository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final TripRepository tripRepository;
    private final FareRuleMapper mapper;
    private final ConfigService configService;
    private final TripService tripService;

    // ========================= CREATE =========================
    @Override
    @Transactional
    public FareRuleDtos.FareRuleResponse create(FareRuleDtos.FareRuleCreateRequest dto) {

        Route route = routeRepository.findById(dto.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(dto.routeId())));

        Stop from = stopRepository.findById(dto.fromStopId())
                .orElseThrow(() -> new NotFoundException("FromStop %d not found".formatted(dto.fromStopId())));

        Stop to = stopRepository.findById(dto.toStopId())
                .orElseThrow(() -> new NotFoundException("ToStop %d not found".formatted(dto.toStopId())));

        FareRule entity = mapper.toEntity(dto);

        entity.setRoute(route);
        entity.setFromStop(from);
        entity.setToStop(to);

        FareRule saved = repository.save(entity);

        return mapper.toResponse(saved);
    }

    // ========================= UPDATE =========================
    @Override
    @Transactional
    public FareRuleDtos.FareRuleResponse update(Long id, FareRuleDtos.FareRuleUpdateRequest dto) {

        FareRule entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));

        mapper.patch(entity, dto);

        Route route = routeRepository.findById(dto.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(dto.routeId())));

        Stop from = stopRepository.findById(dto.fromStopId())
                .orElseThrow(() -> new NotFoundException("FromStop %d not found".formatted(dto.fromStopId())));

        Stop to = stopRepository.findById(dto.toStopId())
                .orElseThrow(() -> new NotFoundException("ToStop %d not found".formatted(dto.toStopId())));

        entity.setRoute(route);
        entity.setFromStop(from);
        entity.setToStop(to);

        return mapper.toResponse(repository.save(entity));
    }

    // ========================= GET =========================
    @Override
    public FareRuleDtos.FareRuleResponse get(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
    }

    // ========================= DELETE =========================
    @Override
    @Transactional
    public void delete(Long id) {
        FareRule entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));

        repository.delete(entity);
    }

    // ========================= QUERIES =========================
    @Override
    public Page<FareRuleDtos.FareRuleResponse> getByRouteId(Long routeId, Pageable pageable) {
        Page<FareRule> page = repository.findByRouteId(routeId, pageable);

        if (page.isEmpty()) {
            throw new NotFoundException("No FareRules found for route %d".formatted(routeId));
        }

        return page.map(mapper::toResponse);
    }

    @Override
    public Page<FareRuleDtos.FareRuleResponse> getByFromStopId(Long stopId, Pageable pageable) {
        Page<FareRule> page = repository.findByFromStopId(stopId, pageable);

        if (page.isEmpty()) {
            throw new NotFoundException("No FareRules found for fromStopId %d".formatted(stopId));
        }

        return page.map(mapper::toResponse);
    }

    @Override
    public Page<FareRuleDtos.FareRuleResponse> getByToStopId(Long stopId, Pageable pageable) {
        Page<FareRule> page = repository.findByToStopId(stopId, pageable);

        if (page.isEmpty()) {
            throw new NotFoundException("No FareRules found for toStopId %d".formatted(stopId));
        }

        return page.map(mapper::toResponse);
    }


    // ========================= CÁLCULO DE PRECIOS =========================

    @Override
    public BigDecimal calculateFare(Long routeId, Long fromStopId, Long toStopId) {
        // Validar que las paradas existan
        Stop fromStop = stopRepository.findById(fromStopId)
                .orElseThrow(() -> new NotFoundException("FromStop %d not found".formatted(fromStopId)));

        Stop toStop = stopRepository.findById(toStopId)
                .orElseThrow(() -> new NotFoundException("ToStop %d not found".formatted(toStopId)));

        // Validar que pertenezcan a la ruta
        if (!fromStop.getRoute().getId().equals(routeId)) {
            throw new IllegalArgumentException("FromStop does not belong to route %d".formatted(routeId));
        }

        if (!toStop.getRoute().getId().equals(routeId)) {
            throw new IllegalArgumentException("ToStop does not belong to route %d".formatted(routeId));
        }

        // Validar orden
        if (fromStop.getOrder() >= toStop.getOrder()) {
            throw new IllegalArgumentException("FromStop order must be less than ToStop order");
        }

        // ✅ CORRECCIÓN: Obtener todas las reglas usando Pageable.unpaged()
        Page<FareRule> rulesPage = repository.findByRouteId(routeId, Pageable.unpaged());
        List<FareRule> rules = rulesPage.getContent();

        FareRule exactRule = rules.stream()
                .filter(rule -> rule.getFromStop().getId().equals(fromStopId)
                        && rule.getToStop().getId().equals(toStopId))
                .findFirst()
                .orElse(null);

        if (exactRule != null) {
            return exactRule.getBasePrice();
        }

        // Si no hay regla exacta, calcular proporcionalmente
        return calculateProportionalFare(routeId, fromStop, toStop);
    }

    /**
     * Calcula la tarifa proporcionalmente basándose en la distancia
     */
    private BigDecimal calculateProportionalFare(Long routeId, Stop fromStop, Stop toStop) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(routeId)));

        // Obtener todas las paradas de la ruta ordenadas
        List<Stop> stops = stopRepository.findByRouteIdOrderByOrderAsc(routeId);

        if (stops.size() < 2) {
            throw new IllegalStateException("Route must have at least 2 stops");
        }

        // Calcular distancia entre paradas (número de paradas intermedias)
        int stopsDistance = toStop.getOrder() - fromStop.getOrder();

        // Obtener precio base por km desde configuración
        BigDecimal pricePerKm = configService.getValue("FARE_PRICE_PER_KM");

        // Calcular distancia estimada (asumiendo distancia uniforme entre paradas)
        BigDecimal totalDistance = route.getDistanceKm();
        int totalStops = stops.size() - 1; // Número de segmentos

        BigDecimal segmentDistance = totalDistance.divide(
                BigDecimal.valueOf(totalStops),
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal tripDistance = segmentDistance.multiply(BigDecimal.valueOf(stopsDistance));

        // Precio = distancia * precio por km
        return tripDistance.multiply(pricePerKm).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal basePrice, String discountType) {
        if (discountType == null || discountType.isBlank()) {
            return basePrice;
        }

        // Obtener porcentaje de descuento desde configuración
        BigDecimal discountPercent;

        try {
            String configKey = "DISCOUNT_" + discountType.toUpperCase() + "_PERCENT";
            discountPercent = configService.getValue(configKey);
        } catch (NotFoundException e) {
            // Si no existe configuración para este descuento, no aplicar nada
            return basePrice;
        }

        // Calcular descuento
        BigDecimal discountAmount = basePrice
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = basePrice.subtract(discountAmount);

        // El precio nunca puede ser negativo
        return finalPrice.max(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calculateDynamicPrice(Long tripId, Long fromStopId, Long toStopId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

        // Calcular precio base
        BigDecimal basePrice = calculateFare(trip.getRoute().getId(), fromStopId, toStopId);

        // ✅ CORRECCIÓN: Obtener todas las reglas usando Pageable.unpaged()
        Page<FareRule> rulesPage = repository.findByRouteId(trip.getRoute().getId(), Pageable.unpaged());
        List<FareRule> rules = rulesPage.getContent();

        boolean hasDynamicPricing = rules.stream()
                .anyMatch(rule -> rule.getFromStop().getId().equals(fromStopId)
                        && rule.getToStop().getId().equals(toStopId)
                        && rule.isDynamicPricing());

        if (!hasDynamicPricing) {
            return basePrice;
        }

        // Calcular ocupación del viaje
        Integer totalSeats = trip.getBus().getCapacity();
        Integer occupiedSeats = tripService.getOccupiedSeatsCount(tripId);

        BigDecimal occupancyRate = BigDecimal.valueOf(occupiedSeats)
                .divide(BigDecimal.valueOf(totalSeats), 4, RoundingMode.HALF_UP);

        // Aplicar ajuste según ocupación
        // 0-50%: Sin ajuste
        // 50-75%: +10%
        // 75-90%: +20%
        // 90-100%: +30%

        BigDecimal multiplier = BigDecimal.ONE;

        if (occupancyRate.compareTo(BigDecimal.valueOf(0.90)) >= 0) {
            multiplier = BigDecimal.valueOf(1.30);
        } else if (occupancyRate.compareTo(BigDecimal.valueOf(0.75)) >= 0) {
            multiplier = BigDecimal.valueOf(1.20);
        } else if (occupancyRate.compareTo(BigDecimal.valueOf(0.50)) >= 0) {
            multiplier = BigDecimal.valueOf(1.10);
        }

        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateFinalPrice(Long tripId, Long fromStopId, Long toStopId, String discountType) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

        // 1. Calcular precio base o dinámico
        BigDecimal price = calculateDynamicPrice(tripId, fromStopId, toStopId);

        // 2. Aplicar descuento si existe
        if (discountType != null && !discountType.isBlank()) {
            price = applyDiscount(price, discountType);
        }

        // 3. Precio mínimo (desde configuración)
        BigDecimal minPrice = configService.getValue("FARE_MINIMUM_PRICE");

        return price.max(minPrice);
    }
}