package application.adapters.security;

import application.adapters.security.mappers.JwtClaimsMapper;

import application.domain.models.Administrator;
import application.domain.models.User;
import application.domain.ports.out.TokenService.TokenClaims;
import application.domain.valueobjects.UserStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests of the JWT adapter of the TokenService output port.
 */
class JwtTokenServiceAdapterTest {

    /** Test-only signing secrets (32+ bytes). */
    private static final String SECRET = "test-secret-for-unit-tests-only-0123456789";
    private static final String OTHER_SECRET = "another-test-secret-for-unit-tests-9876543210";

    private final JwtTokenServiceAdapter tokenService = new JwtTokenServiceAdapter(SECRET, 3_600_000);
    private final User user = new User(
            new Administrator("admin-1", "Ada Admin", "admin@x.com", UserStatus.ACTIVE), "hashed:x");

    @Test
    void issuedTokenCarriesOnlyIdentityClaims() {
        String token = tokenService.issueToken(user);
        TokenClaims claims = tokenService.parseToken(token).orElseThrow();
        assertEquals("admin-1", claims.subject());
        assertEquals(0, claims.tokenVersion());
        assertTrue(claims.expiresAt().isAfter(claims.issuedAt()));
        assertEquals(3600, tokenService.expirationSeconds());

        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        assertFalse(payload.contains("admin@x.com"));
        assertFalse(payload.contains("ADMINISTRATOR"));
        assertFalse(payload.contains("hashed"));
    }

    @Test
    void tokenReflectsTheCurrentVersion() {
        user.invalidateSessions();
        assertEquals(1, tokenService.parseToken(tokenService.issueToken(user)).orElseThrow().tokenVersion());
    }

    @Test
    void tamperedMalformedOrForeignTokensAreRejected() {
        String token = tokenService.issueToken(user);
        assertTrue(tokenService.parseToken(token + "x").isEmpty());
        assertTrue(tokenService.parseToken("not-a-jwt").isEmpty());
        assertTrue(tokenService.parseToken("").isEmpty());
        assertTrue(tokenService.parseToken(null).isEmpty());
        String foreign = new JwtTokenServiceAdapter(OTHER_SECRET, 3_600_000).issueToken(user);
        assertTrue(tokenService.parseToken(foreign).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() {
        Instant past = Instant.now().minusSeconds(120);
        String expired = Jwts.builder()
                .subject("admin-1")
                .claim(JwtClaimsMapper.VERSION_CLAIM, 0)
                .issuedAt(Date.from(past.minusSeconds(60)))
                .expiration(Date.from(past))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertTrue(tokenService.parseToken(expired).isEmpty());
    }

    @Test
    void shortSecretsAndInvalidLifetimesAreRejectedAtStartup() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenServiceAdapter("too-short", 1000));
        assertThrows(IllegalStateException.class, () -> new JwtTokenServiceAdapter(SECRET, 0));
    }
}
