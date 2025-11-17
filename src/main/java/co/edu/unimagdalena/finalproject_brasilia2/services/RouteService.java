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
    List<RouteResponse> listByOrigin(String origin);
    List<RouteResponse> listByDestination(String destination);
    List<RouteResponse> listByOriginAndDestination(String origin, String destination);
    List<RouteResponse> listByDurationMinBetween(Integer min, Integer max);
    Page<RouteResponse> listByDurationMinLessThanEqual(Integer min, Pageable pageable);
    Page<RouteResponse> listByDistanceKmLessThanEqual(BigDecimal distanceKm, Pageable pageable);
    Page<RouteResponse> listByDistanceKmGreaterThanEqual(BigDecimal distanceKm, Pageable pageable);
}
