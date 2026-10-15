package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.InventoryMovementResponse;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.MarketplaceWarehouse;
import application.domain.models.SellerWarehouse;
import application.domain.models.Warehouse;

/**
 * Mapper: Domain Models (Warehouse, Inventory, InventoryMovement) to Response DTOs.
 */
public final class WarehouseDtoMapper {

    private WarehouseDtoMapper() {
    }

    public static WarehouseResponse toResponse(Warehouse warehouse) {
        String ownership;
        String ownerId;
        if (warehouse instanceof SellerWarehouse sellerWarehouse) {
            ownership = "SELLER";
            ownerId = sellerWarehouse.getOwner().getIdentifier();
        } else if (warehouse instanceof MarketplaceWarehouse marketplaceWarehouse) {
            ownership = "MARKETPLACE";
            ownerId = marketplaceWarehouse.getManagedBy().getIdentifier();
        } else {
            ownership = warehouse.getClass().getSimpleName();
            ownerId = null;
        }
        return new WarehouseResponse(
                warehouse.getIdentifier(),
                warehouse.getName(),
                PersonDtoMapper.toResponse(warehouse.getAddress()),
                ownership,
                ownerId);
    }

    public static InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getIdentifier(),
                inventory.getProduct().getIdentifier(),
                inventory.getWarehouse().getIdentifier(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getDamagedQuantity());
    }

    public static InventoryMovementResponse toResponse(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getMovementId(),
                movement.getMovementType().getCode(),
                movement.getQuantity(),
                movement.getMovementDate().toString(),
                movement.getPerformedBy().getIdentifier());
    }
}
