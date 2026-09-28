package Application.adapters.in.rest.responses;

/**
 * Response DTO: stock of a product at a warehouse.
 */
public record InventoryResponse(String identifier, String productId, String warehouseId,
                                int availableQuantity, int reservedQuantity, int damagedQuantity) {
}
