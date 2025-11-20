package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PassengerType;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

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



    @Override
    public BigDecimal calculateTicketPrice(Long tripId, Long fromStopId, Long toStopId, PassengerType passengerType) {
        // 1. Validar trip y obtener stops
        var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

        var fromStop = stopRepository.findById(fromStopId)
                .orElseThrow(() -> new NotFoundException("FromStop %d not found".formatted(fromStopId)));

        var toStop = stopRepository.findById(toStopId)
                .orElseThrow(() -> new NotFoundException("ToStop %d not found".formatted(toStopId)));

        // Validar que stops pertenezcan a la ruta del trip
        if (!fromStop.getRoute().getId().equals(trip.getRoute().getId())) {
            throw new IllegalArgumentException("FromStop doesn't belong to trip's route");
        }
        if (!toStop.getRoute().getId().equals(trip.getRoute().getId())) {
            throw new IllegalArgumentException("ToStop doesn't belong to trip's route");
        }
        if (fromStop.getOrder() >= toStop.getOrder()) {
            throw new IllegalArgumentException("FromStop order must be less than ToStop order");
        }

        // 2. Buscar FareRule exacta o calcular proporcional
        var rulesPage = repository.findByRouteId(trip.getRoute().getId(), Pageable.unpaged());
        var rules = rulesPage.getContent();

        var exactRule = rules.stream()
                .filter(rule -> rule.getFromStop().getId().equals(fromStopId)
                        && rule.getToStop().getId().equals(toStopId))
                .findFirst()
                .orElse(null);

        BigDecimal basePrice;
        boolean hasDynamicPricing = false;

        if (exactRule != null) {
            basePrice = exactRule.getBasePrice();
            hasDynamicPricing = exactRule.isDynamicPricing();
        } else {
            // Calcular proporcional
            var route = trip.getRoute();
            var stops = stopRepository.findByRouteIdOrderByOrderAsc(route.getId());
            var stopsDistance = toStop.getOrder() - fromStop.getOrder();
            var pricePerKm = configService.getValue("FARE_PRICE_PER_KM");
            var segmentDistance = route.getDistanceKm().divide(
                    BigDecimal.valueOf(stops.size() - 1), 2, RoundingMode.HALF_UP);
            basePrice = segmentDistance.multiply(BigDecimal.valueOf(stopsDistance))
                    .multiply(pricePerKm).setScale(2, RoundingMode.HALF_UP);
        }

        // 3. Aplicar pricing dinámico si está habilitado
        if (hasDynamicPricing) {
            var totalSeats = trip.getBus().getCapacity();
            var occupiedSeats = tripService.getOccupiedSeatsCount(tripId);
            var occupancyRate = BigDecimal.valueOf(occupiedSeats)
                    .divide(BigDecimal.valueOf(totalSeats), 4, RoundingMode.HALF_UP);

            BigDecimal multiplier = BigDecimal.ONE;
            if (occupancyRate.compareTo(BigDecimal.valueOf(0.90)) >= 0) {
                multiplier = BigDecimal.valueOf(1.30); // +30%
            } else if (occupancyRate.compareTo(BigDecimal.valueOf(0.75)) >= 0) {
                multiplier = BigDecimal.valueOf(1.20); // +20%
            } else if (occupancyRate.compareTo(BigDecimal.valueOf(0.50)) >= 0) {
                multiplier = BigDecimal.valueOf(1.10); // +10%
            }
            basePrice = basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        }

        // 4. Aplicar descuento por tipo de pasajero
        if (passengerType != PassengerType.ADULT) {
            try {
                var configKey = "DISCOUNT_" + passengerType.name() + "_PERCENT";
                var discountPercent = configService.getValue(configKey);
                var discountAmount = basePrice.multiply(discountPercent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                basePrice = basePrice.subtract(discountAmount);
            } catch (NotFoundException e) {
                // Si no existe config para este descuento, ignorar
            }
        }

        // 5. Aplicar precio mínimo
        var minPrice = configService.getValue("FARE_MINIMUM_PRICE");
        return basePrice.max(minPrice);
    }
}