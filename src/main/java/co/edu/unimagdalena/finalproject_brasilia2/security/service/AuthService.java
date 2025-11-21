package co.edu.unimagdalena.finalproject_brasilia2.security.service;

import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalproject_brasilia2.security.dto.AuthDtos.*; // Importar los records internos
import co.edu.unimagdalena.finalproject_brasilia2.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Creamos el UserDetails temporal
        var userDetails = new UserDetailsImpl(user);
        String token = jwtService.generateToken(userDetails);

        // Retornamos la respuesta con el formato nuevo
        return new AuthResponse(token, "Bearer", user.getRole().name());
    }
}