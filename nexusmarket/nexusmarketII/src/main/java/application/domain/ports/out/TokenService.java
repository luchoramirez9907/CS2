package application.domain.ports.out;

import application.domain.models.User;

import java.time.Instant;
import java.util.Optional;

/**
 * Output port: issues and validates access tokens (e.g. JWT). Tokens only
 * carry identity claims; role and status are always read from the current
 * User, never from the token.
 */
public interface TokenService {

    /**
     * @return a signed access token for the user, bound to its current
     *         authTokenVersion
     */
    String issueToken(User user);

    /**
     * @return the claims of a valid token, or empty when the token is
     *         malformed, tampered with or expired
     */
    Optional<TokenClaims> parseToken(String token);

    /**
     * @return lifetime of the issued tokens, in seconds
     */
    long expirationSeconds();

    /**
     * Identity claims carried by an access token.
     *
     * @param subject      identifier of the Person owning the account
     * @param tokenId      unique identifier of the token
     * @param tokenVersion authTokenVersion of the User when the token was issued
     */
    record TokenClaims(String subject, String tokenId, int tokenVersion,
                       Instant issuedAt, Instant expiresAt) {
    }
}
