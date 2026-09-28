package Application.adapters.in.rest.requests;

/**
 * Request DTO: PUT /api/carts/items/{productId}
 */
public record UpdateCartItemRequest(int quantity) {
}
