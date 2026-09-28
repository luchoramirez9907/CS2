package Application.domain.ports.in;

import Application.domain.models.Warehouse;
import Application.domain.valueobjects.Address;

/**
 * Input port (use case): consults and updates an existing warehouse.
 */
public interface ManageWarehouseUseCase {

    Warehouse consultWarehouse(String requesterId, String warehouseId);

    Warehouse updateWarehouse(String requesterId, String warehouseId, String name, Address address);
}
