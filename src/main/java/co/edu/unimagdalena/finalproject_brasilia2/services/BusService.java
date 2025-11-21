package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos;
import org.springframework.data.domain.Page;

public interface BusService {
        BusDtos.BusResponse create(BusDtos.BusCreateRequest request);
        BusDtos.BusResponse update(Long id, BusDtos.BusUpdateRequest request);
        BusDtos.BusResponse get(Long id);
        void delete(Long id);

        BusDtos.BusResponse getByLicensePlate(String licensePlate);
        Page<BusDtos.BusResponse> getByCapacityGreaterThanEqual(Integer capacity, int page, int size);
        Page<BusDtos.BusResponse> getByCapacityLessThanEqual(Integer capacity, int page, int size);
        Page<BusDtos.BusResponse> getByCapacityBetween(Integer minCapacity, Integer maxCapacity, int page, int size);

}
