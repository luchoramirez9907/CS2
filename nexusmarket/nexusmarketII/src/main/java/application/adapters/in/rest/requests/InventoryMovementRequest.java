package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST .../inventory/movements
 * movementType: InventoryMovementType code. quantity: positive, except for
 * ADJUSTMENT, which accepts a signed correction (validated by the domain).
 */
public record InventoryMovementRequest(@NotBlank @Size(max = 64) String productId,
                                       @NotBlank @Size(max = 64) String warehouseId,
                                       @NotBlank @Pattern(regexp = "STOCK_IN|RESERVATION|RESERVATION_RELEASE|SALE_EXIT"
                                               + "|ADJUSTMENT|RETURN|DAMAGE") String movementType,
                                       @Min(-100000) @Max(100000) int quantity) {
}
