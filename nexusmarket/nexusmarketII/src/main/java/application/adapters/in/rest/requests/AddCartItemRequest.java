package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST /api/v1/buyer/cart/items
 * The unit price is taken from the catalog price of the product.
 */
public record AddCartItemRequest(@NotBlank @Size(max = 64) String productId,
                                 @Positive @Max(1000) int quantity) {
}
