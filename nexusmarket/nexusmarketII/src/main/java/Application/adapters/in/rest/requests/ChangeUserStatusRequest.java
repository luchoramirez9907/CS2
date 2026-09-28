package Application.adapters.in.rest.requests;

/**
 * Request DTO: change the operational status of a user.
 */
public record ChangeUserStatusRequest(String performerId, String userId, String action) {
}
