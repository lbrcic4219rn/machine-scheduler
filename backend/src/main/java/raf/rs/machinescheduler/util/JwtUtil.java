package raf.rs.machinescheduler.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import raf.rs.machinescheduler.model.User;
import raf.rs.machinescheduler.services.UserService;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final Duration TOKEN_LIFETIME = Duration.ofMillis(10000L * 60 * 60 * 10);

    private final UserService userService;
    private final SecretKey secretKey;

    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(this.secretKey).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        return this.extractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().toInstant().isBefore(Instant.now());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        User user = userService.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with email: " + username + " not found"));
        claims.put("permissions", user.getPermission());
        Instant now = Instant.now();
        claims.put(Claims.ISSUED_AT, now.getEpochSecond());
        claims.put(Claims.EXPIRATION, now.plus(TOKEN_LIFETIME).getEpochSecond());
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .signWith(this.secretKey).compact();
    }

    public boolean validateToken(String token, UserDetails user) {
        try {
            return (user.getUsername().equals(extractUsername(token)) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
