package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: PUT .../warehouses/{warehouseId}. address: null keeps the current value.
 */
public record UpdateWarehouseRequest(@NotBlank @Size(max = 160) String name,
                                     @Valid AddressRequest address) {
}
