package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.CashClosureDtos.*;

import java.time.LocalDate;
import java.util.List;

public interface CashClosureService {
    CashClosureResponse create(CashClosureCreateRequest request);
    CashClosureResponse get(Long id);
    CashClosureResponse getByClerkAndDate(Long clerkId, LocalDate date);
    List<CashClosureResponse> getByClerk(Long clerkId);
    List<CashClosureResponse> getByDateRange(LocalDate startDate, LocalDate endDate);
}