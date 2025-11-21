package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentDtos {
    public record PaymentCreateRequest(
            @NotNull Long ticketId,
            @NotNull @Positive BigDecimal amount,
            @NotNull PaymentMethod paymentMethod,
            String transactionId,
            String paymentReference,
            String notes
    ) implements Serializable {}

    public record PaymentUpdateRequest(
            PaymentStatus status,
            String transactionId,
            String paymentReference,
            String notes
    ) implements Serializable {}

    public record PaymentResponse(
            Long id,
            Long ticketId,
            String ticketQrCode,
            String passengerName,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            OffsetDateTime paymentDate,
            String transactionId,
            String paymentReference,
            String notes
    ) implements Serializable {}
}

