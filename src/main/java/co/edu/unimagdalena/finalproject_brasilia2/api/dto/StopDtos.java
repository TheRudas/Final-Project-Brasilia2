package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

public class StopDtos {
    public record StopCreateRequest(@NotNull Long routeId, @NotBlank String name, @NotNull @Positive Integer order,
    @NotNull Double lat, @NotNull Double lng) implements Serializable {}

    public record StopUpdateRequest(String name, Integer order, Double lat, Double lng) implements Serializable {}

    public record StopResponse(Long id, Long routeId, String name, Integer order, Double lat, Double lng) implements Serializable {}
}