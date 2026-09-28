package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/orders/{orderId}/shipment
 */
public record CreateShipmentRequest(String originWarehouseId, String logisticsOperatorId) {
}
