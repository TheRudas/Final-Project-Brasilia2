package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.StopService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.StopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StopServiceImpl implements StopService {
    private final StopRepository stopRepository;
    private final StopMapper mapper;
    private final RouteRepository routeRepository;
    @Override
    @Transactional
    public StopResponse create(StopCreateRequest request) {
        var route = routeRepository.findById(request.routeId()).
                orElseThrow(() -> new NotFoundException("Route with id %d not found".formatted(request.routeId())));

        //validate if already exists this order for this route.
        if(stopRepository.existsByRouteIdAndOrder(request.routeId(), request.order())) {
            throw new IllegalArgumentException("Stop order %d already exists for route %d".formatted(request.order(), request.routeId()));
        }
        var stop = mapper.toEntity(request);
        stop.setRoute(route);
        return mapper.toResponse(stopRepository.save(stop));
    }

    @Override
    @Transactional
    public StopResponse update(Long id, StopUpdateRequest request) {
        var stop = stopRepository.findById(id).orElseThrow(() -> new NotFoundException("Stop with id %d not found".formatted(id)));

        // validate order uniqueness if a joker change this
        if (request.order() != null && !request.order().equals(stop.getOrder())) {
            if (stopRepository.existsByRouteIdAndOrder(stop.getRoute().getId(), request.order())) {
                throw new IllegalArgumentException("Stop order %d already exists for this route".formatted(request.order()));
            }
        }
        mapper.patch(stop, request);
        return mapper.toResponse(stopRepository.save(stop));
    }

    @Override
    public StopResponse get(Long id) {
        return mapper.toResponse(stopRepository.findById(id).orElseThrow(() -> new NotFoundException("Stop with id %d not found".formatted(id))));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var stop = stopRepository.findById(id).orElseThrow(() -> new NotFoundException("Stop with id %d not found".formatted(id)));
        stopRepository.delete(stop);
    }

    @Override
    public StopResponse getByNameIgnoreCase(String name) {
        return stopRepository.findByNameIgnoreCase(name).map(mapper::toResponse).
                orElseThrow(() -> new NotFoundException("Stop with name %s not found".formatted(name)));

    }

    @Override
    public List<StopResponse> listByRouteId(Long routeId) {
        List<Stop> stops = stopRepository.findByRouteId(routeId);
        if (stops.isEmpty()) {
            throw new NotFoundException("No stops found for route %d".formatted(routeId));
        }
        return stops.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<StopResponse> listByRouteIdOrderByOrderAsc(Long routeId) {
        List<Stop> stops = stopRepository.findByRouteIdOrderByOrderAsc(routeId);
        if (stops.isEmpty()) {
            throw new NotFoundException("No stops found for route %d".formatted(routeId));
        }
        return stops.stream().map(mapper::toResponse).toList();
    }

    @Override
    public StopResponse getByRouteIdAndNameIgnoreCase(Long routeId, String name) {
        return stopRepository.findByRouteIdAndNameIgnoreCase(routeId, name).map(mapper::toResponse).orElseThrow(
                () -> new NotFoundException("Route with id %d and name \"%s\" not found".formatted(routeId, name))
        );
    }

    @Override
    public StopResponse getByRouteIdAndOrder(Long routeId, Integer order) {
        return stopRepository.findByRouteIdAndOrder(routeId, order).map(mapper::toResponse).orElseThrow(
                ()-> new NotFoundException("Route with id %d and order %d not found".formatted(routeId, order))
        );
    }
}
