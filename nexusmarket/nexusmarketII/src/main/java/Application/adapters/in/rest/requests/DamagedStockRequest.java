package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/inventory/damaged
 */
public record DamagedStockRequest(String productId, String warehouseId, int quantity) {
}
