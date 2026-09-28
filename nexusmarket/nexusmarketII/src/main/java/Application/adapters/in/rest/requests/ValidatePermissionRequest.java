package Application.adapters.in.rest.requests;

/**
 * Request DTO: validate whether a user may perform a business operation.
 */
public record ValidatePermissionRequest(String userId, String operation) {
}
