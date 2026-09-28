package Application.adapters.in.rest.requests;

/**
 * Request DTO: register an inventory movement (stock-in or adjustment).
 */
public record InventoryMovementRequest(String performerId, String productId,
                                       String warehouseId, int quantity,
                                       String movementType) {
}
