package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/returns
 */
public record RequestReturnRequest(String buyerId, String orderId, String reason) {
}
