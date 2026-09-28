package Application.domain.ports.in;

import Application.domain.models.Inventory;

/**
 * Input port (use case): Register Inventory Movement.
 *
 * Records significant movements affecting an inventory record — initial
 * stock, manual adjustments and return reinstatements — ensuring that
 * stock is never reserved or reduced below zero and that every change
 * generates a movement in the history.
 */
public interface RegisterInventoryMovementUseCase {

    /**
     * Registers initial stock for a product at a warehouse, creating the
     * inventory record when it does not exist yet.
     *
     * @param performerId identifier of the LogisticsOperator
     * @param productId   identifier of the stocked product
     * @param warehouseId identifier of the receiving warehouse
     * @param quantity    quantity entering the warehouse (positive)
     * @return the affected Inventory record
     */
    Inventory registerStockIn(String performerId, String productId, String warehouseId, int quantity);

    /**
     * Applies a manual correction of the recorded stock. The delta is
     * signed and must never drive the available stock negative.
     */
    Inventory registerAdjustment(String performerId, String productId, String warehouseId, int signedDelta);
}
