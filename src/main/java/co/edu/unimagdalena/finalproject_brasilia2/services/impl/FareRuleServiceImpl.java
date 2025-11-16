package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.FareRuleRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.FareRuleService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.FareRuleMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository repository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final FareRuleMapper mapper;

    // ========================= CREATE =========================
    @Override
    @Transactional
    public FareRuleDtos.FareRuleResponse create(FareRuleDtos.FareRuleCreateRequest dto) {

        Route route = routeRepository.findById(dto.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(dto.routeId())));

        Stop from = stopRepository.findById(dto.fromStopId())
                .orElseThrow(() -> new NotFoundException("FromStop %d not found".formatted(dto.fromStopId())));

        Stop to = stopRepository.findById(dto.toStopId())
                .orElseThrow(() -> new NotFoundException("ToStop %d not found".formatted(dto.toStopId())));

        FareRule entity = mapper.toEntity(dto);

        entity.setRoute(route);
        entity.setFromStop(from);
        entity.setToStop(to);

        FareRule saved = repository.save(entity);

        return mapper.toResponse(saved);
    }

    // ========================= UPDATE =========================
    @Override
    @Transactional
    public FareRuleDtos.FareRuleResponse update(Long id, FareRuleDtos.FareRuleUpdateRequest dto) {

        FareRule entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));

        mapper.patch(entity, dto);

        Route route = routeRepository.findById(dto.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(dto.routeId())));

        Stop from = stopRepository.findById(dto.fromStopId())
                .orElseThrow(() -> new NotFoundException("FromStop %d not found".formatted(dto.fromStopId())));

        Stop to = stopRepository.findById(dto.toStopId())
                .orElseThrow(() -> new NotFoundException("ToStop %d not found".formatted(dto.toStopId())));

        entity.setRoute(route);
        entity.setFromStop(from);
        entity.setToStop(to);

        return mapper.toResponse(repository.save(entity));
    }

    // ========================= GET =========================
    @Override
    public FareRuleDtos.FareRuleResponse get(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
    }

    // ========================= DELETE =========================
    @Override
    @Transactional
    public void delete(Long id) {
        FareRule entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));

        repository.delete(entity);
    }

    // ========================= QUERY #1 =========================

    // PERO NO RECIBE routeId --> así que devolvemos TODOS (paginados)
    @Override
    public Page<FareRuleDtos.FareRuleResponse> getByRouteId(Pageable pageable) {
        Page<FareRule> page = repository.findAll(pageable);

        if (page.isEmpty()) {
            throw new NotFoundException("No FareRules found");
        }

        return page.map(mapper::toResponse);
    }

    // ========================= QUERY #2 =========================
    @Override
    public Page<FareRuleDtos.FareRuleResponse> getByFromStopId(Pageable pageable, Long stopId) {
        Page<FareRule> page = repository.findByFromStopId(stopId, pageable);

        if (page.isEmpty()) {
            throw new NotFoundException("No FareRules found for fromStopId %d".formatted(stopId));
        }

        return page.map(mapper::toResponse);
    }

    // ========================= QUERY #3 =========================
    @Override
    public Page<FareRuleDtos.FareRuleResponse> getByToStopId(Pageable pageable, Long stopId) {
        Page<FareRule> page = repository.findByToStopId(stopId, pageable);

        if (page.isEmpty()) {
            throw new NotFoundException("No FareRules found for toStopId %d".formatted(stopId));
        }

        return page.map(mapper::toResponse);
    }
}
