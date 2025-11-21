package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

public class    BusDtos {
    public record BusCreateRequest(String licensePlate, int capacity, boolean status) implements java.io.Serializable {}
    public record BusUpdateRequest(String licensePlate, Integer capacity, boolean status) implements java.io.Serializable {}
    public record BusResponse(Long id, String licensePlate, int capacity, boolean status, java.time.OffsetDateTime createdAt) implements java.io.Serializable {}
}
