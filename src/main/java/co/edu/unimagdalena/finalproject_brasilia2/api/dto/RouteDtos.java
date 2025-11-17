package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;

public class RouteDtos {
    public record RouteCreateRequest(@NotBlank String code, @NotBlank String name, @NotBlank String origin, @NotBlank String destination,
                                     @Positive BigDecimal distanceKm, @Positive Integer durationMin) implements Serializable {}

    public record RouteUpdateRequest(String name, String origin, String destination, @Positive BigDecimal distanceKm, @Positive Integer durationMin) implements Serializable {}

    public record RouteResponse(Long id, String code, String name, String origin, String destination, BigDecimal distanceKm,
                                Integer durationMin) implements Serializable {}
}
