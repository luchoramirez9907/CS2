package Application.adapters.in.rest.requests;

/**
 * Request DTO: PUT /api/users/{id}, PUT /api/sellers/{id}
 */
public record UpdatePersonRequest(String fullName, String email) {
}
