package Application.adapters.in.rest.requests;

/**
 * Request DTO: update the quantity of a cart item.
 */
public record UpdateCartItemRequest(String buyerId, String cartId, String productId, int quantity) {
}
