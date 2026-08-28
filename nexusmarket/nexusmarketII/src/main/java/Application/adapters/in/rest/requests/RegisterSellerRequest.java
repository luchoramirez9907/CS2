package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/sellers
 */
public record RegisterSellerRequest(String administratorId, String identifier,
                                    String fullName, String email) {
}
