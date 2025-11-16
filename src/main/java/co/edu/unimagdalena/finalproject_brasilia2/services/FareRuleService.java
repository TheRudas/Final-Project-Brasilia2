package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FareRuleService {
    //create, update, get, delete
    FareRuleDtos.FareRuleResponse create(FareRuleDtos.FareRuleCreateRequest  request);
    FareRuleDtos.FareRuleResponse update(Long id, FareRuleDtos.FareRuleUpdateRequest request);
    FareRuleDtos.FareRuleResponse get(Long id);
    void delete(Long id);

    Page<FareRuleDtos.FareRuleResponse> getByRouteId(Pageable pageable);
    Page<FareRuleDtos.FareRuleResponse> getByFromStopId(Pageable pageable, Long stopId);
    Page<FareRuleDtos.FareRuleResponse> getByToStopId(Pageable pageable, Long stopId);
}
