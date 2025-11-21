package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class CashClosureDtos {

    public record CashClosureCreateRequest(
            @NotNull Long clerkId,
            @NotNull LocalDate closureDate,
            @NotNull @PositiveOrZero BigDecimal totalCash,
            @NotNull Integer totalTicketsSold,
            @NotNull Integer totalParcelsRegistered,
            @NotNull @PositiveOrZero BigDecimal totalBaggageFees,
            String notes
    ) implements Serializable {}

    public record CashClosureResponse(
            Long id,
            Long clerkId,
            String clerkName,
            LocalDate closureDate,
            BigDecimal totalCash,
            Integer totalTicketsSold,
            Integer totalParcelsRegistered,
            BigDecimal totalBaggageFees,
            BigDecimal totalRevenue,
            String notes,
            OffsetDateTime createdAt
    ) implements Serializable {}
}