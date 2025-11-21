package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentDtos {

    /**
     * Request to create a payment
     * Note: Ticket is created as SOLD only when payment is confirmed
     */
    public record PaymentCreateRequest(
            @NotNull Long ticketId,
            @NotNull @Positive BigDecimal amount,
            @NotNull PaymentMethod method,
            String referenceCode
    ) implements Serializable {}

    /**
     * Request to confirm a payment (QR/Transfer)
     */
    public record PaymentConfirmRequest(
            @NotBlank String transactionId,
            String paymentProofUrl
    ) implements Serializable {}

    /**
     * Payment response
     */
    public record PaymentResponse(
            Long id,
            Long ticketId,
            BigDecimal amount,
            PaymentMethod method,
            boolean confirmed,
            boolean pending,
            boolean failed,
            String transactionId,
            String referenceCode,
            String paymentProofUrl,
            OffsetDateTime createdAt,
            OffsetDateTime confirmedAt,
            String failureReason
    ) implements Serializable {}
}