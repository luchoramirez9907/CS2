package Application.domain.ports.in;

import Application.domain.models.Inventory;
import Application.domain.valueobjects.InventoryMovementType;

/**
 * Input port (use case): records a significant movement affecting the
 * inventory record of a product at a warehouse. Stock is never reserved
 * or reduced below zero, and damaged stock can never be reserved.
 */
public interface RegisterInventoryMovementUseCase {

    /**
     * @param type     STOCK_IN (initial/incoming stock; creates the record when missing),
     *                 RESERVATION (positive quantity reserves, negative releases),
     *                 ADJUSTMENT (signed manual correction) or RETURN (reinstatement)
     * @param quantity quantity affected by the movement
     * @return the updated Inventory record
     */
    Inventory registerMovement(String requesterId, String productId, String warehouseId,
                               InventoryMovementType type, int quantity);

    /**
     * Marks available stock as damaged; damaged units cannot be reserved.
     */
    Inventory registerDamagedStock(String requesterId, String productId, String warehouseId,
                                   int quantity);
}
