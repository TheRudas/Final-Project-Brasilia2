package co.edu.unimagdalena.finalproject_brasilia2.security.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank String name,        // En tu entidad se llama 'name', en la ref 'userName'
            @NotBlank @Email String email,
            @NotNull UserRole role,       // Tu Enum del proyecto
            @NotBlank @Size(min = 8) String password,
            @NotBlank String phone
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String token,
            String tokenType,
            String role // Cambiado de 'long' a 'String' porque tu rol es un Enum (ADMIN, DRIVER, etc.)
    ) {}
}