package application.adapters.security;

import application.adapters.security.dtos.JwtClaimsDTO;
import application.adapters.security.mappers.JwtClaimsMapper;
import application.domain.models.User;
import application.domain.ports.out.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import javax.crypto.SecretKey;

/**
 * Adapter implementing the TokenService output port with HS256-signed JWT
 * (JJWT). The claims are built and read by JwtClaimsMapper (JwtClaimsDTO).
 * The token only carries identity claims: sub (person identifier), jti (token id), ver (User.authTokenVersion), iat and exp. It never
 * contains the role, email or any other personal data.
 */
@Component
public class JwtTokenServiceAdapter implements TokenService {

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenServiceAdapter(@Value("${jwt.secret}") String secret,
                                  @Value("${jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("jwt.secret must have at least " + MIN_SECRET_BYTES + " bytes");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("jwt.expiration-ms must be positive");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    @Override
    public String issueToken(User user) {
        JwtClaimsDTO claims = JwtClaimsMapper.toDto(user, Instant.now(), expirationMs);
        return JwtClaimsMapper.applyClaims(Jwts.builder(), claims)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public Optional<TokenClaims> parseToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return JwtClaimsMapper.toDto(claims).map(JwtClaimsMapper::toTokenClaims);
        } catch (JwtException | IllegalArgumentException ex) {
            // Malformed, tampered with or expired token.
            return Optional.empty();
        }
    }

    @Override
    public long expirationSeconds() {
        return expirationMs / 1000;
    }
}
