package Application.adapters.in.rest.requests;

/**
 * Request DTO: change the operational status of a seller.
 */
public record ChangeSellerStatusRequest(String performerId, String sellerId, String action) {
}
