package Application.adapters.in.rest.responses;

/**
 * Response DTO: inventory stock of a product at a warehouse.
 */
public record InventoryResponse(String inventoryId, String productId, String warehouseId,
                                int availableQuantity, int reservedQuantity, int totalQuantity) {
}
