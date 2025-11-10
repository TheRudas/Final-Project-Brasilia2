package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class SeatDtos {
    public record SeatCreateRequest(@NotNull Long busId, @NotBlank String number, @NotNull SeatType seatType) implements Serializable {}

    public record SeatUpdateRequest(String number, SeatType seatType) implements Serializable {}

    public record SeatResponse(Long id, Long busId, String number, SeatType seatType) implements Serializable {}
}
