package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.InventoryResponse;
import Application.adapters.in.rest.responses.WarehouseResponse;
import Application.domain.models.Inventory;
import Application.domain.models.MarketplaceWarehouse;
import Application.domain.models.SellerWarehouse;
import Application.domain.models.Warehouse;

/**
 * Mapper: Domain Models (Warehouse, Inventory) to Response DTOs.
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
}
