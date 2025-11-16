package co.edu.unimagdalena.finalproject_brasilia2.services;


import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParcelService {
    ParcelDtos.ParcelResponse create(ParcelDtos.ParcelCreateRequest request);
    ParcelDtos.ParcelResponse update(Long id, ParcelDtos.ParcelUpdateRequest request);
    ParcelDtos.ParcelResponse get(Long id);
    void delete(Long id);
    // metodos
    // ====================== QUERIES ======================
    List<ParcelDtos.ParcelResponse> getBySenderName(String senderName);

    List<ParcelDtos.ParcelResponse> getBySenderPhone(String senderPhone);

    List<ParcelDtos.ParcelResponse> getByReceiverName(String receiverName);

    List<ParcelDtos.ParcelResponse> getByReceiverPhone(String receiverPhone);

    List<ParcelDtos.ParcelResponse> getByFromStopId(Long stopId);

    List<ParcelDtos.ParcelResponse> getByToStopId(Long stopId);
}
