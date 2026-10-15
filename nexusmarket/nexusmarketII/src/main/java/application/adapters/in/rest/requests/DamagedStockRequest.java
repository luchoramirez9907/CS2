package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST .../inventory/damaged
 */
public record DamagedStockRequest(@NotBlank @Size(max = 64) String productId,
                                  @NotBlank @Size(max = 64) String warehouseId,
                                  @Positive @Max(100000) int quantity) {
}
