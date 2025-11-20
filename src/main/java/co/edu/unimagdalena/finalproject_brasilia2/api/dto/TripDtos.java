package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class TripDtos {
    public record TripCreateRequest(@NotNull Long routeId, @NotNull Long busId, @NotNull OffsetDateTime localDate,@NotBlank OffsetDateTime departureTime,@NotBlank OffsetDateTime arrivalTime) implements java.io.Serializable {}
    public record TripUpdateRequest(Long routeId, Long busId, LocalDate localDate, OffsetDateTime departureTime, OffsetDateTime arrivalTime, TripStatus status) implements java.io.Serializable {}
    public record TripResponse(Long id, Long routeId, Long busId, LocalDate localDate, OffsetDateTime departureTime, OffsetDateTime arrivalTime, TripStatus status) implements java.io.Serializable {}
    

}
