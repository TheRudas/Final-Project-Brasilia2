package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class ParcelDtos {
    public record ParcelCreateRequest(@NotBlank String Code, @NotBlank String senderName, @NotBlank String senderPhone, @NotBlank String receiverName, @NotBlank String receiverPhone, @NotNull Long fromStopId,@NotNull Long toStopId)implements Serializable {}
    public record ParcelUpdateRequest(String senderName, String senderPhone, String receiverName, String receiverPhone, Long fromStopId, Long toStopId) implements Serializable {}
    public record ParcelResponse(Long id, String code, String senderName, String senderPhone, String receiverName, String receiverPhone, Long fromStopId, Long toStopId) implements Serializable {}

}
