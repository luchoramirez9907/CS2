package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.InventoryResponse;
import Application.domain.models.Inventory;

/**
 * Mapper: Domain Model (Inventory) to Response DTO.
 */
public final class InventoryDtoMapper {

    private InventoryDtoMapper() {
    }

    public static InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getIdentifier(),
                inventory.getProduct().getIdentifier(),
                inventory.getWarehouse().getIdentifier(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getTotalQuantity());
    }
}
