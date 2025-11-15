package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatHoldStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class SeatHoldDtos {

    public record SeatHoldCreateRequest(@NotNull Long tripId, @NotBlank String seatNumber, @NotNull Long userId) implements Serializable {}

    //Update? El seathold es inmutable una vez creado, al menos eso pienso yo.

    public record SeatHoldResponse(Long id, Long tripId, String seatNumber, Long userId, String userName,
                                   OffsetDateTime expiresAt, SeatHoldStatus status) implements Serializable {}
    }