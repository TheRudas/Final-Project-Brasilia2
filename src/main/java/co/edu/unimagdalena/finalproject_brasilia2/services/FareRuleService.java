package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PassengerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface FareRuleService {
    // CRUD
    FareRuleDtos.FareRuleResponse create(FareRuleDtos.FareRuleCreateRequest request);
    FareRuleDtos.FareRuleResponse update(Long id, FareRuleDtos.FareRuleUpdateRequest request);
    FareRuleDtos.FareRuleResponse get(Long id);
    void delete(Long id);

    Page<FareRuleDtos.FareRuleResponse> getByRouteId(Long routeId, Pageable pageable);
    Page<FareRuleDtos.FareRuleResponse> getByFromStopId(Long stopId, Pageable pageable);
    Page<FareRuleDtos.FareRuleResponse> getByToStopId(Long stopId, Pageable pageable);

    BigDecimal calculateTicketPrice(Long tripId, Long fromStopId, Long toStopId, PassengerType passengerType);
}