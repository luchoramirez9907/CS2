package Application.domain.ports.in;

/**
 * Input port (use case): Consult Inventory.
 *
 * Retrieves the current available and reserved stock of a product at a
 * specific warehouse.
 */
public interface ConsultInventoryUseCase {

    record StockSnapshot(String inventoryId, String productId, String warehouseId,
                         int availableQuantity, int reservedQuantity) {
    }

    StockSnapshot consultStock(String requesterId, String productId, String warehouseId);
}
