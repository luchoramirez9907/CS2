package Application.adapters.in.rest.responses;

/**
 * Response DTO: registered seller information.
 */
public record SellerResponse(String identifier, String fullName, String email,
                             String role, String status, String registeredBy) {
}
