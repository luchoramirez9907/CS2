package Application.adapters.in.rest.requests;

/**
 * Request DTO: validate whether a user may access an owned resource.
 */
public record ValidateOwnershipRequest(String userId, String resourceType, String resourceId) {
}
