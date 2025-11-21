package co.edu.unimagdalena.finalproject_brasilia2.security.web;

import co.edu.unimagdalena.finalproject_brasilia2.security.dto.AuthDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.security.service.AuthService;
import co.edu.unimagdalena.finalproject_brasilia2.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 * Handles user registration, login, token refresh, and current user info
 *
 * @author AFGamero
 * @since 2025-11-21
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user
     * POST /api/auth/register
     * Public endpoint - no authentication required
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login existing user
     * POST /api/auth/login
     * Public endpoint - no authentication required
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current authenticated user information
     * GET /api/auth/me
     * Requires authentication
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var user = userDetails.getUser();

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );

        return ResponseEntity.ok(userInfo);
    }

    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     * Public endpoint - validates refresh token internally
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout (client-side token removal)
     * POST /api/auth/logout
     * Note: With JWT stateless authentication, logout is handled by the client
     * by removing the tokens from storage
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // JWT stateless: el cliente debe eliminar los tokens
        // No hay sesión en el servidor que invalidar
        return ResponseEntity.ok().build();
    }
}