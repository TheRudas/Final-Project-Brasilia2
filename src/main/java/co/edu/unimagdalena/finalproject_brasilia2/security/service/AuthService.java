package co.edu.unimagdalena.finalproject_brasilia2.security.service;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalproject_brasilia2.security.dto.AuthDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        // Validate email doesn't exist
        if (userRepository.existsByEmail((request.email()))) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Validate phone doesn't exist
        if (userRepository.existsByPhone((request.phone()))) {
            throw new IllegalArgumentException("Phone already registered");
        }

        // Create user
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .role(request.role())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {} with role {}", user.getEmail(), user.getRole());

        // Generate tokens
        var userDetails = new UserDetailsImpl(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtService.getExpirationSeconds(),
                new UserInfo(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole())
        );
    }

    /**
     * Login existing user
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Load user
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if account is active
        if (!user.isStatus()) {
            log.warn("Login attempt for deactivated account: {}", request.email());
            throw new IllegalStateException("Account is deactivated. Please contact support.");
        }

        log.info("User logged in successfully: {} with role {}", user.getEmail(), user.getRole());

        // Generate tokens
        var userDetails = new UserDetailsImpl(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtService.getExpirationSeconds(),
                new UserInfo(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole())
        );
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refresh(RefreshTokenRequest request) {
        log.info("Refresh token request received");

        String email = jwtService.extractUsername(request.refreshToken());

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var userDetails = new UserDetailsImpl(user);

        if (!jwtService.isTokenValid(request.refreshToken(), userDetails)) {
            log.warn("Invalid refresh token for user: {}", email);
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Check if account is still active
        if (!user.isStatus()) {
            log.warn("Refresh attempt for deactivated account: {}", email);
            throw new IllegalStateException("Account is deactivated");
        }

        log.info("Tokens refreshed successfully for user: {}", email);

        // Generate new tokens
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtService.getExpirationSeconds(),
                new UserInfo(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole())
        );
    }
}