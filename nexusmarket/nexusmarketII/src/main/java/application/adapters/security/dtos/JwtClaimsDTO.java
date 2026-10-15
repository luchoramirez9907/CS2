package application.adapters.security.dtos;

import java.time.Instant;

/**
 * Claims carried inside the signed JWT. It is the contract of the security
 * adapter: the domain never sees JWT claims, only JwtClaimsMapper converts
 * User &lt;-&gt; this DTO &lt;-&gt; token claims.
 *
 * <p>The token only carries identity: no role, email or other personal
 * data, so authorization is always resolved from the current User.
 *
 * @param subject      identifier of the Person owning the account (sub)
 * @param tokenId      unique identifier of the token (jti)
 * @param tokenVersion User.authTokenVersion when the token was issued (ver)
 * @param issuedAt     issue instant (iat)
 * @param expiresAt    expiration instant (exp)
 */
public record JwtClaimsDTO(String subject, String tokenId, int tokenVersion,
                           Instant issuedAt, Instant expiresAt) {
}
