package co.edu.unimagdalena.finalproject_brasilia2.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    private final SecretKey key;
    private final Long expirationSeconds;
    private final Long refreshExpirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:86400}") Long expirationSeconds,
            @Value("${security.jwt.refresh-expiration-seconds:604800}") Long refreshExpirationSeconds) {
        // La clave debe tener al menos 32 caracteres (256 bits)
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationSeconds = expirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    /**
     * Generate access token with default claims
     */
    public String generateToken(UserDetails principal) {
        return generateToken(principal, new HashMap<>());
    }

    /**
     * Generate access token with custom claims
     */
    public String generateToken(UserDetails principal, Map<String, Object> extraClaims) {
        var now = Instant.now();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(principal.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate refresh token (longer expiration)
     */
    public String generateRefreshToken(UserDetails principal) {
        var now = Instant.now();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpirationSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validate token against user details
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parse all claims from token
     */
    private Claims parseAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get access token expiration time in seconds
     */
    public Long getExpirationSeconds() {
        return expirationSeconds;
    }

    /**
     * Get refresh token expiration time in seconds
     */
    public Long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }
}