package co.edu.unimagdalena.finalproject_brasilia2.services;


import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface RouteService {
    RouteResponse create(RouteCreateRequest request);
    RouteResponse update(Long id, RouteUpdateRequest request);
    RouteResponse get(Long id);
    void delete(Long id);

    RouteResponse getByCode(String code);
    RouteResponse getByName(String name);
    List<RouteResponse> getByOrigin(String origin);
    List<RouteResponse> getByDestination(String destination);
    List<RouteResponse> getByOriginAndDestination(String origin, String destination);
    List<RouteResponse> getByDurationMinBetween(Integer min, Integer max);
    Page<RouteResponse> getByDurationMinLessThanEqual(Integer min, Pageable pageable);
    Page<RouteResponse> getByDistanceKmLessThanEqual(BigDecimal distanceKm, Pageable pageable);
    Page<RouteResponse> getByDistanceKmGreaterThanEqual(BigDecimal distanceKm, Pageable pageable);
}
