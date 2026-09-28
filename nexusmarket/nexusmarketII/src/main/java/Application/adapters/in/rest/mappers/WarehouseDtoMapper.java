package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.WarehouseResponse;
import Application.domain.models.MarketplaceWarehouse;
import Application.domain.models.SellerWarehouse;
import Application.domain.models.Warehouse;

/**
 * Mapper: Domain Model (Warehouse) to Response DTO.
 */
public final class WarehouseDtoMapper {

    private WarehouseDtoMapper() {
    }

    public static WarehouseResponse toResponse(Warehouse warehouse) {
        String managedBy = warehouse instanceof MarketplaceWarehouse marketplace
                ? marketplace.getManagedBy().getIdentifier() : null;
        String ownerSeller = warehouse instanceof SellerWarehouse seller
                ? seller.getOwner().getIdentifier() : null;
        String ownership = warehouse instanceof MarketplaceWarehouse ? "MARKETPLACE" : "SELLER";
        return new WarehouseResponse(
                warehouse.getIdentifier(),
                warehouse.getName(),
                ownership,
                warehouse.getAddress().getStreet(),
                warehouse.getAddress().getCity(),
                warehouse.getAddress().getState(),
                warehouse.getAddress().getCountry(),
                warehouse.getAddress().getPostalCode(),
                managedBy,
                ownerSeller);
    }
}
