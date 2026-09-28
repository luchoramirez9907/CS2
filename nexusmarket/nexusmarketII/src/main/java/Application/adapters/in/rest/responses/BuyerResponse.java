package Application.adapters.in.rest.responses;

/**
 * Response DTO: buyer information.
 */
public record BuyerResponse(String identifier, String fullName, String email,
                            String role, String status, String commercialStatus) {
}
