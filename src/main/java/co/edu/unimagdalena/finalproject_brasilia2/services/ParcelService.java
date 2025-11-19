package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;

import java.util.List;

public interface ParcelService {
    ParcelDtos.ParcelResponse create(ParcelDtos.ParcelCreateRequest request);
    ParcelDtos.ParcelResponse update(Long id, ParcelDtos.ParcelUpdateRequest request);
    ParcelDtos.ParcelResponse get(Long id);
    void delete(Long id);

    // Queries existentes
    List<ParcelDtos.ParcelResponse> getBySenderName(String senderName);
    List<ParcelDtos.ParcelResponse> getBySenderPhone(String senderPhone);
    List<ParcelDtos.ParcelResponse> getByReceiverName(String receiverName);
    List<ParcelDtos.ParcelResponse> getByReceiverPhone(String receiverPhone);
    List<ParcelDtos.ParcelResponse> getByFromStopId(Long stopId);
    List<ParcelDtos.ParcelResponse> getByToStopId(Long stopId);

    // ⭐ AGREGAR ESTOS MÉTODOS

    /**
     * Buscar por código
     */
    ParcelDtos.ParcelResponse getByCode(String code);

    /**
     * Buscar por estado
     */
    List<ParcelDtos.ParcelResponse> getByStatus(ParcelStatus status);

    /**
     * Entrega un paquete validando el OTP
     */
    ParcelDtos.ParcelResponse deliverParcel(Long parcelId, String otp);

    /**
     * Asigna un paquete a un viaje
     */
    ParcelDtos.ParcelResponse assignToTrip(Long parcelId, Long tripId);

    /**
     * Cambia el estado de un paquete
     */
    ParcelDtos.ParcelResponse updateStatus(Long parcelId, ParcelStatus newStatus);

    /**
     * Lista paquetes para entregar en una parada
     */
    List<ParcelDtos.ParcelResponse> listParcelsForDelivery(Long stopId);
}