package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.StopCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.StopResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.StopUpdateRequest;

import java.util.List;

public interface StopService {
    StopResponse create(StopCreateRequest request);
    StopResponse update(Long id, StopUpdateRequest request);
    StopResponse get(Long id);
    void delete(Long id);

    StopResponse getByNameIgnoreCase(String name);
    List<StopResponse> listByRouteId(Long routeId);
    List<StopResponse> listByRouteIdOrderByOrderAsc(Long routeId);
    StopResponse getByRouteIdAndNameIgnoreCase(Long routeId, String name);
    StopResponse getByRouteIdAndOrder(Long routeId, Integer order);
}
