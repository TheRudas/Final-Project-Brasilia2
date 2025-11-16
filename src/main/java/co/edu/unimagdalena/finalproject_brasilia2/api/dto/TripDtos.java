package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TripDtos {
    public record TripCreateRequest(@NotNull Long routeId, @NotNull Long busId, @NotBlank String localDate,@NotBlank String departureTime,@NotBlank String arrivalTime) implements java.io.Serializable {}
    public record tripUpdateRequest(Long routeId, Long busId,String localDate, String departureTime, String arrivalTime) implements java.io.Serializable {}
    public record TripResponse(Long id, Long routeId, Long busId,String localDate, String departureTime, String arrivalTime) implements java.io.Serializable {}
    

}
