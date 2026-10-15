package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO: PUT /api/v1/seller/products/{productId}
 * price and variants: null keeps the current value.
 */
public record UpdateProductRequest(@NotBlank @Size(max = 160) String name,
                                   @Size(max = 1000) String description,
                                   @PositiveOrZero @Digits(integer = 13, fraction = 2) BigDecimal price,
                                   List<PublishProductRequest.@Valid VariantRequest> variants) {
}
