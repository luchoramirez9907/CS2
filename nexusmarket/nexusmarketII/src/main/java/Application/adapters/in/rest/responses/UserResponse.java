package Application.adapters.in.rest.responses;

/**
 * Response DTO: system participant information.
 */
public record UserResponse(String identifier, String fullName, String email, String role,
                           String status) {
}
