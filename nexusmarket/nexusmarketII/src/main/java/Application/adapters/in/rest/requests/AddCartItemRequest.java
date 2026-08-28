package Application.adapters.in.rest.requests;

import java.math.BigDecimal;

/**
 * Request DTO: POST /api/carts/items
 */
public record AddCartItemRequest(String buyerId, String cartId, String productId,
                                 int quantity, BigDecimal unitPrice) {
}
