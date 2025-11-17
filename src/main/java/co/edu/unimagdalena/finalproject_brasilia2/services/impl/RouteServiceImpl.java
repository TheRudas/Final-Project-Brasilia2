package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.RouteService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.RouteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final RouteRepository routeRepository;
    private final RouteMapper mapper;
    
    @Override
    @Transactional
    public RouteResponse create(RouteCreateRequest request) {
        if (routeRepository.existsByCode(request.code()))
        {
            throw new IllegalStateException("Route with code %s already exists".formatted(request.code()));
        }
        var route = mapper.toEntity(request);
        route.setCode(request.code());
        return mapper.toResponse(routeRepository.save(route));
    }

    @Override
    @Transactional
    public RouteResponse update(Long id, RouteUpdateRequest request) {
        var route = routeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(id)));

        mapper.patch(route, request);
        return mapper.toResponse(routeRepository.save(route));
    }

    @Override
    public RouteResponse get(Long id) {
        return routeRepository.findById(id).map(mapper::toResponse).orElseThrow(() -> new NotFoundException("Route %d not found".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var route = routeRepository.findById(id).orElseThrow(() -> new NotFoundException("Route %d not found or was deleted yet".formatted(id)));
        routeRepository.delete(route);
    }

    @Override
    public RouteResponse getByCode(String code) {
        return routeRepository.findByCode(code).map(mapper::toResponse).orElseThrow(() -> new NotFoundException("Route with code %s not found".formatted(code)));
    }

    @Override
    public RouteResponse getByName(String name) {
        return routeRepository.findByName(name).map(mapper::toResponse).orElseThrow(() -> new NotFoundException("Route with name %s not found".formatted(name)));
    }

    @Override
    public List<RouteResponse> listByOrigin(String origin) {
        List<Route> routes = routeRepository.findByOrigin(origin);
        if (routes.isEmpty())
            throw new NotFoundException("Route from origin %s not found".formatted(origin));
        return routes.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<RouteResponse> listByDestination(String destination) {
        List<Route> routes = routeRepository.findByDestination(destination);
        if (routes.isEmpty())
            throw new NotFoundException("Route to destination %s not found".formatted(destination));
        return routes.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<RouteResponse> listByOriginAndDestination(String origin, String destination) {
        List<Route> routes = routeRepository.findByOriginAndDestination(origin, destination);
        if (routes.isEmpty())
            throw new NotFoundException("Route from %s to %s not found".formatted(origin, destination));
        return routes.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<RouteResponse> listByDurationMinBetween(Integer min, Integer max) {
        List<Route> routes = routeRepository.findByDurationMinBetween(min, max);
        if (routes.isEmpty())
            throw new NotFoundException("No routes found with duration between %d and %d minutes".formatted(min, max));
        return routes.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Page<RouteResponse> listByDurationMinLessThanEqual(Integer min, Pageable pageable) {
        Page<Route> routes = routeRepository.findByDurationMinLessThanEqual(min, pageable);
        if (routes.isEmpty()) {
            throw new NotFoundException("No routes found with duration <= %d minutes".formatted(min));
        }
        return routes.map(mapper::toResponse);
    }

    @Override
    public Page<RouteResponse> listByDistanceKmLessThanEqual(BigDecimal distanceKm, Pageable pageable) {
        Page<Route>  routes = routeRepository.findByDistanceKmLessThanEqual(distanceKm, pageable);
        if (routes.isEmpty()) {
            throw new NotFoundException("No routes found with distance <= %s km".formatted(distanceKm));
        }
        return routes.map(mapper::toResponse);
    }

    @Override
    public Page<RouteResponse> listByDistanceKmGreaterThanEqual(BigDecimal distanceKm, Pageable pageable) {
        Page<Route> routes = routeRepository.findByDistanceKmGreaterThanEqual(distanceKm, pageable);
        if (routes.isEmpty()) {
            throw new NotFoundException("No routes found with distance >= %s km".formatted(distanceKm));
        }
        return routes.map(mapper::toResponse);
    }
}
