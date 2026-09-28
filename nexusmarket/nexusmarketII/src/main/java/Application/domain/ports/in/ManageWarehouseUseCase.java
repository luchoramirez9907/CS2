package Application.domain.ports.in;

import Application.domain.models.Warehouse;

/**
 * Input port (use case): Manage Warehouse.
 *
 * Consults and updates the information of an existing warehouse
 * according to the applicable business rules.
 */
public interface ManageWarehouseUseCase {

    Warehouse consultWarehouse(String requesterId, String warehouseId);

    /**
     * @param performerId identifier of the Administrator or owning Seller
     * @param warehouseId identifier of the warehouse to update
     * @param newName     new display name of the warehouse
     * @return the updated Warehouse
     */
    Warehouse updateWarehouse(String performerId, String warehouseId, String newName);
}
