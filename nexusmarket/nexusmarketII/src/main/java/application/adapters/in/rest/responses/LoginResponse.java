package application.adapters.in.rest.responses;

/**
 * Response DTO: POST /api/v1/auth/login
 *
 * @param token     signed access token to send as "Authorization: Bearer <token>"
 * @param tokenType always "Bearer"
 * @param expiresIn lifetime of the token, in seconds
 * @param user      the authenticated participant
 */
public record LoginResponse(String token, String tokenType, long expiresIn, UserResponse user) {

    /** Never exposes the token (e.g. in logs). */
    @Override
    public String toString() {
        return "LoginResponse{tokenType='" + tokenType + "', expiresIn=" + expiresIn + ", user=" + user + "}";
    }
}
