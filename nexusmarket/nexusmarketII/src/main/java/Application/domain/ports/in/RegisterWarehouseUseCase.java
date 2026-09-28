package Application.domain.ports.in;

import Application.domain.models.Warehouse;
import Application.domain.valueobjects.Address;

/**
 * Input port (use case): creates a new warehouse, owned either by the
 * Marketplace or by a seller.
 */
public interface RegisterWarehouseUseCase {

    /**
     * @param requesterId   Administrator (any ownership) or Seller (own warehouse only)
     * @param ownerSellerId owner seller; null or blank for a Marketplace warehouse
     *                      (managed by the requesting Administrator)
     * @return the registered Warehouse
     */
    Warehouse registerWarehouse(String requesterId, String identifier, String name,
                                Address address, String ownerSellerId);
}
