package Application.domain.ports.in;

import Application.domain.models.Inventory;

/**
 * Input port (use case): retrieves the current available and reserved
 * stock of a product at a specific warehouse.
 */
public interface ConsultInventoryUseCase {

    Inventory consultInventory(String requesterId, String productId, String warehouseId);
}
