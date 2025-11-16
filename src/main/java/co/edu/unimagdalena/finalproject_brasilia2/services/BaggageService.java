package co.edu.unimagdalena.finalproject_brasilia2.services;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface BaggageService {
    BaggageResponse create(BaggageCreateRequest request);
    BaggageResponse update(Long id, BaggageUpdateRequest request);
    BaggageResponse get(Long id);
    void delete(Long id);

    BaggageResponse getByTagCode(String tagCode);
    List<BaggageResponse> getByPassengerId(Long passengerId);
    Page<BaggageResponse> getByWeightGreaterThanOrEqual(BigDecimal weightKg, Pageable pageable);
    Page<BaggageResponse> getByWeightLessThanOrEqual(BigDecimal weightKg, Pageable pageable);
    Page<BaggageResponse> getByWeightBetween(BigDecimal minKg, BigDecimal maxKg, Pageable pageable);
    List<BaggageResponse> getAllByTicketId(Long ticketId);
}
