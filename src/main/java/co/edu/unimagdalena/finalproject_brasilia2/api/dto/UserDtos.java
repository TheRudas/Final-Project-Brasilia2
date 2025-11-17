package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class UserDtos {
    public record UserCreateRequest(@NotBlank String name, @NotBlank @Email String email,
                                    @NotBlank @Size(min = 10, max = 10) String phone, @NotNull UserRole role, @NotBlank String password) implements Serializable {}
    public record UserUpdateRequest(String name, @Email String email, @Size(min = 10, max = 10) String phone, UserRole role, boolean status) implements Serializable {}

    public record UserResponse(Long id, String name, String email, String phone, UserRole role, boolean status, OffsetDateTime createdAt) implements Serializable {}
}
