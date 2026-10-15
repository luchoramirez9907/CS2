package application.domain.models;

/**
 * AuthenticationResult
 *
 * Result of a successful login: the authenticated User and the signed
 * access token issued for it.
 *
 * @param user             the authenticated access account
 * @param accessToken      signed access token (e.g. JWT)
 * @param expiresInSeconds lifetime of the token, in seconds
 */
public record AuthenticationResult(User user, String accessToken, long expiresInSeconds) {

    public AuthenticationResult {
        if (user == null) {
            throw new IllegalArgumentException("An AuthenticationResult requires the authenticated User");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("An AuthenticationResult requires an access token");
        }
    }

    /** Never exposes the access token (e.g. in logs). */
    @Override
    public String toString() {
        return "AuthenticationResult{user=" + user + ", expiresInSeconds=" + expiresInSeconds + "}";
    }
}
