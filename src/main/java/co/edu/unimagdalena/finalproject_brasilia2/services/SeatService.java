package co.edu.unimagdalena.finalproject_brasilia2.services;


import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;

import java.util.List;

public interface SeatService {
    SeatResponse create(SeatCreateRequest request);
    SeatResponse update(Long id, SeatUpdateRequest request);
    SeatResponse get(Long id);
    void delete(Long id);

    List<SeatResponse> listByBusId(Long busId);
    List<SeatResponse> listByBusIdAndSeatType(Long busId, SeatType seatType);
    SeatResponse getByBusIdAndNumber(Long busId, String number);
    List<SeatResponse> listByBusIdOrderByNumberAsc(Long busId);
    Long countByBusId(Long busId);
}
