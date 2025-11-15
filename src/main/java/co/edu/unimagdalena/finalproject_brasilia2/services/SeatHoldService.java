package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;

import java.util.List;

public interface SeatHoldService {
    SeatHoldResponse create(SeatHoldCreateRequest request);
    SeatHoldResponse get(Long id);
    List<SeatHoldResponse> getByTripId(Long tripId);
    List<SeatHoldResponse> getByUserId(Long userId);
    void expire(Long id);
    void expireAll();
}
