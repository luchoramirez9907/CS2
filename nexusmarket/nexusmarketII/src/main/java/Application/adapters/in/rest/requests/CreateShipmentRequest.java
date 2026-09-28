package Application.adapters.in.rest.requests;

/**
 * Request DTO: create the shipment of a paid order.
 */
public record CreateShipmentRequest(String operatorId, String orderId, String originWarehouseId) {
}
