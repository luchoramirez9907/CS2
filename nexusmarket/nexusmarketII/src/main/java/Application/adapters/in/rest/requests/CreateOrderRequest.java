package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/orders
 */
public record CreateOrderRequest(String buyerId, String cartId) {
}
