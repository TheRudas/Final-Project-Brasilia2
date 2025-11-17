package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface TicketService {
    TicketResponse create(TicketCreateRequest request);
    TicketResponse update(Long id, TicketUpdateRequest request);
    TicketResponse get(Long id);
    void delete(Long id);
    TicketResponse getByQrCode(String qrCode);
    List<TicketResponse> listByPassengerId(Long passengerId);
    List<TicketResponse> listByTripId(Long tripId);
    Page<TicketResponse> listByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    Page<TicketResponse> listByStatus(TicketStatus status, Pageable pageable);
    Page<TicketResponse> listBetweenStops(Long fromStopId, Long toStopId, Pageable pageable);
    BigDecimal getTotalByPassengerId(Long passengerId);
    TicketResponse cancel(Long id);
}
