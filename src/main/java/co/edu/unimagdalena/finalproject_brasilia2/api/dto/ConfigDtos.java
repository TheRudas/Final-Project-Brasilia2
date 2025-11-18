package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class ConfigDtos {

    public record ConfigCreateRequest(@NotBlank String key, @NotBlank String value) implements Serializable {}

    public record ConfigUpdateRequest(@NotBlank String value) implements Serializable {}

    public record ConfigResponse(String key, String value) implements Serializable {}
}
