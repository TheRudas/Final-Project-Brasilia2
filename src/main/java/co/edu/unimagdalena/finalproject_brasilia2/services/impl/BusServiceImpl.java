package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.BusService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.BusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusServiceImpl implements BusService {

    private final BusRepository repository;
    private final BusMapper mapper;

    @Override
    @Transactional
    public BusDtos.BusResponse create(BusDtos.BusCreateRequest request) {
        if (repository.findByPlate(request.licensePlate()).isPresent()) {
            throw new IllegalStateException(
                    "A bus with plate %s already exists".formatted(request.licensePlate())
            );


        }
        Bus entity = mapper.toEntity(request);
        Bus saved = repository.save(entity);

        return mapper.toResponse(saved);
    }
    @Override
    public BusDtos.BusResponse update(BusDtos.BusUpdateRequest request) {
        // El update está basado en la plate
        Bus existing = repository.findByPlate(request.licensePlate())
                .orElseThrow(() -> new NotFoundException(
                        "Bus with plate %s not found".formatted(request.licensePlate())
                ));

        // Patch estilo MapStruct
        mapper.patch(existing, request);

        return mapper.toResponse(repository.save(existing));
    }

    @Override
    public BusDtos.BusResponse get(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() ->
                        new NotFoundException("Bus %d not found".formatted(id))
                );
    }

    @Override
    public void delete(Long id) {
        Bus bus = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Bus %d not found or already deleted".formatted(id))
                );

        repository.delete(bus);
    }

    @Override
    public BusDtos.BusResponse getByLicensePlate(String licensePlate) {
        return repository.findByPlate(licensePlate)
                .map(mapper::toResponse)
                .orElseThrow(() ->
                        new NotFoundException("Bus with plate %s not found".formatted(licensePlate))
                );
    }

    @Override
    public Page<BusDtos.BusResponse> getByCapacityGreaterThanEqual(Integer capacity, int page, int size) {
        Page<Bus> result = repository.findByCapacityGreaterThanEqual(capacity, PageRequest.of(page, size));

        if (result.isEmpty()) {
            throw new NotFoundException("No buses found with capacity >= %d".formatted(capacity));
        }

        return result.map(mapper::toResponse);
    }

    @Override
    public Page<BusDtos.BusResponse> getByCapacityLessThanEqual(Integer capacity, int page, int size) {
        Page<Bus> result = repository.findByCapacityLessThanEqual(capacity, PageRequest.of(page, size));

        if (result.isEmpty()) {
            throw new NotFoundException("No buses found with capacity <= %d".formatted(capacity));
        }

        return result.map(mapper::toResponse);
    }

    @Override
    public Page<BusDtos.BusResponse> getByCapacityBetween(Integer minCapacity, Integer maxCapacity, int page, int size) {
        Page<Bus> result = repository.findByCapacityBetween(minCapacity, maxCapacity, PageRequest.of(page, size));

        if (result.isEmpty()) {
            throw new NotFoundException(
                    "No buses found between capacities %d and %d".formatted(minCapacity, maxCapacity)
            );
        }

        return result.map(mapper::toResponse);
    }
}
