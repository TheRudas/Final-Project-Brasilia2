package co.edu.unimagdalena.finalproject_brasilia2.security.config;

import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalproject_brasilia2.security.error.Http401EntryPoint;
import co.edu.unimagdalena.finalproject_brasilia2.security.error.Http403AccessDenied;
import co.edu.unimagdalena.finalproject_brasilia2.security.jwt.JwtAuthenticationFilter;
import co.edu.unimagdalena.finalproject_brasilia2.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration using HYBRID approach:
 * - SecurityConfig: Defines PUBLIC endpoints and default (authenticated)
 * - @PreAuthorize: Handles role-based authorization logic
 *
 * This provides:
 * ✅ Clean and readable configuration
 * ✅ Business logic visible in controllers
 * ✅ Fail-safe default (authenticated)
 *
 * @author AFGamero
 * @since 2025-11-21
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ✅ Enables @PreAuthorize, @PostAuthorize, @Secured
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;
    private final Http401EntryPoint unauthorizedHandler;
    private final Http403AccessDenied accessDeniedHandler;

    // ===================== BEANS =====================

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===================== SECURITY FILTER CHAIN =====================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                // Custom error handlers (JSON responses)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler)  // 401
                        .accessDeniedHandler(accessDeniedHandler)       // 403
                )

                // Stateless session (JWT)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ========== AUTHORIZATION RULES ==========
                .authorizeHttpRequests(auth -> auth
                        // ========== PUBLIC ENDPOINTS ==========
                        // Authentication (anyone can register/login)
                        .requestMatchers("/api/auth/**").permitAll()

                        // API Documentation (optional, can restrict in prod)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Public catalog (users can search trips before registering)
                        .requestMatchers(HttpMethod.GET, "/api/routes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stops/route/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/trips/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/trips/filter").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/fare-rules/calculate").permitAll()

                        // ========== FAIL-SAFE DEFAULT ==========
                        // All other endpoints require authentication
                        // Specific role-based authorization is handled by @PreAuthorize in controllers
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}