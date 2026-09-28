package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/users (header X-User-Id: administrator)
 * role: ADMINISTRATOR, SUPERVISOR or LOGISTICS_OPERATOR.
 */
public record RegisterUserRequest(String identifier, String fullName, String email, String role) {
}
