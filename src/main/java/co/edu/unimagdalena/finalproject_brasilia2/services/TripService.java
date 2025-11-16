package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.TripResponse;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;

import java.util.List;

public interface TripService {

    TripResponse create(TripCreateRequest request);

    TripResponse update(Long id, TripUpdateRequest request);

    TripResponse get(Long id);

    void delete(Long id);

    // ====================== QUERIES ======================

    List<TripResponse> findByStatusAndBusId(TripStatus status, Long busId);
}
