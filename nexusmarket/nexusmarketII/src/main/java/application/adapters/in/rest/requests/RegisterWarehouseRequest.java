package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST .../warehouses
 * ownerSellerId (administrators only): null registers a Marketplace
 * warehouse. Sellers always register warehouses they own.
 */
public record RegisterWarehouseRequest(@NotBlank @Size(max = 64) @Pattern(regexp = "\\S+") String identifier,
                                       @NotBlank @Size(max = 160) String name,
                                       @NotNull @Valid AddressRequest address,
                                       @Size(max = 64) String ownerSellerId) {
}
