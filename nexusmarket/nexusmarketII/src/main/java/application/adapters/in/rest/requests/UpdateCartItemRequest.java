package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO: PUT /api/v1/buyer/cart/items/{productId}
 */
public record UpdateCartItemRequest(@Positive @Max(1000) int quantity) {
}
