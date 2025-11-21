package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PassengerType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TicketDtos {
    public record TicketCreateRequest(@NotNull Long tripId, @NotNull Long passengerId, @NotBlank String seatNumber,
                                      @NotNull Long fromStopId, @NotNull Long toStopId, @Positive BigDecimal price,
                                      @NotNull PaymentMethod paymentMethod, @NotNull PassengerType passengerType) implements Serializable {}

    public record TicketUpdateRequest(String seatNumber, @Positive BigDecimal price, PaymentMethod paymentMethod,
                                      TicketStatus status) implements Serializable {}

    public record TicketResponse(Long id, Long tripId, Long passengerId, String passengerName, String busPlate, OffsetDateTime departureAt,
                                 String seatNumber, Long fromStopId, Long toStopId, BigDecimal price, PaymentMethod paymentMethod,
                                 TicketStatus status, String qrCode, BigDecimal noShowFee, BigDecimal refundAmount, PassengerType passengerType) implements Serializable {}

}
