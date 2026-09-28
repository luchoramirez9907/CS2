package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/inventory/movements
 * movementType: STOCK_IN, RESERVATION (negative quantity releases), ADJUSTMENT or RETURN.
 */
public record InventoryMovementRequest(String productId, String warehouseId, String movementType,
                                       int quantity) {
}
