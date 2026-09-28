package Application.adapters.in.rest.responses;

/**
 * Response DTO: authorization validation result.
 */
public record AuthorizationResponse(String userId, boolean allowed, String detail) {
}
