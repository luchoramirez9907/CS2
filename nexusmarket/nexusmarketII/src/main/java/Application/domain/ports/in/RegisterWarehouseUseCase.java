package Application.domain.ports.in;

import Application.domain.models.Warehouse;
import Application.domain.valueobjects.Address;

/**
 * Input port (use case): Register Warehouse.
 *
 * Creates a new warehouse, either owned directly by the Marketplace
 * (registered by an Administrator) or owned by a seller (registered by
 * the seller itself), according to the specified ownership.
 */
public interface RegisterWarehouseUseCase {

    Warehouse registerMarketplaceWarehouse(String performerId, String warehouseId,
                                           String name, Address address);

    Warehouse registerSellerWarehouse(String performerId, String warehouseId,
                                      String name, Address address);
}
