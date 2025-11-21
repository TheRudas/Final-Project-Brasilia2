package co.edu.unimagdalena.finalproject_brasilia2.security.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    /**
     * Request for user registration
     */
    public record RegisterRequest(
            @NotBlank(message = "Name is required")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotNull(message = "Role is required")
            UserRole role,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            @NotBlank(message = "Phone is required")
            @Size(min = 10, max = 10, message = "Phone must be exactly 10 digits")
            String phone
    ) {}

    /**
     * Request for user login
     */
    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    /**
     * Request for refreshing access token
     */
    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ) {}

    /**
     * Authentication response with tokens and user info
     */
    public record AuthResponse(
            String token,
            String refreshToken,
            String tokenType,
            String role,
            Long expiresIn,
            UserInfo user
    ) {
        public static AuthResponse of(String token, String refreshToken, Long expiresIn, UserInfo user) {
            return new AuthResponse(
                    token,
                    refreshToken,
                    "Bearer",
                    user.role().name(),
                    expiresIn,
                    user
            );
        }
    }

    /**
     * User information DTO
     */
    public record UserInfo(
            Long id,
            String name,
            String email,
            String phone,
            UserRole role
    ) {}
}