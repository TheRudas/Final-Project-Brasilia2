package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

public class BaggageDtos {
    public record BaggageCreateRequest(
            @NotNull Long ticketId, @NotNull @Positive BigDecimal weightKg) implements Serializable {}

    public record BaggageUpdateRequest(@Positive BigDecimal weightKg, @PositiveOrZero BigDecimal fee) implements Serializable {}

    public record BaggageResponse(Long id, Long ticketId, String passengerName, BigDecimal weightKg, BigDecimal fee, String tagCode) implements Serializable {}
}