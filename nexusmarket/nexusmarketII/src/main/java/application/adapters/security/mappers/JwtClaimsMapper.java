package application.adapters.security.mappers;

import application.adapters.security.dtos.JwtClaimsDTO;
import application.domain.models.User;
import application.domain.ports.out.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Mapping between the domain User, the JwtClaimsDTO and the JWT claims.
 * Every conversion domain &lt;-&gt; claims lives here, not in the token
 * adapter nor in the domain.
 */
public final class JwtClaimsMapper {

    public static final String VERSION_CLAIM = "ver";

    private JwtClaimsMapper() {
    }

    /**
     * @return the claims of a new token for the user, bound to its current
     *         authTokenVersion
     */
    public static JwtClaimsDTO toDto(User user, Instant issuedAt, long expirationMs) {
        return new JwtClaimsDTO(user.getIdentifier(), UUID.randomUUID().toString(),
                user.getAuthTokenVersion(), issuedAt, issuedAt.plusMillis(expirationMs));
    }

    public static JwtBuilder applyClaims(JwtBuilder builder, JwtClaimsDTO dto) {
        return builder
                .subject(dto.subject())
                .id(dto.tokenId())
                .claim(VERSION_CLAIM, dto.tokenVersion())
                .issuedAt(Date.from(dto.issuedAt()))
                .expiration(Date.from(dto.expiresAt()));
    }

    /**
     * @return the DTO of verified claims, or empty when the subject or the
     *         version claim is missing
     */
    public static Optional<JwtClaimsDTO> toDto(Claims claims) {
        Integer version = claims.get(VERSION_CLAIM, Integer.class);
        if (claims.getSubject() == null || version == null
                || claims.getIssuedAt() == null || claims.getExpiration() == null) {
            return Optional.empty();
        }
        return Optional.of(new JwtClaimsDTO(claims.getSubject(), claims.getId(), version,
                claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()));
    }

    public static TokenService.TokenClaims toTokenClaims(JwtClaimsDTO dto) {
        return new TokenService.TokenClaims(dto.subject(), dto.tokenId(), dto.tokenVersion(),
                dto.issuedAt(), dto.expiresAt());
    }
}
