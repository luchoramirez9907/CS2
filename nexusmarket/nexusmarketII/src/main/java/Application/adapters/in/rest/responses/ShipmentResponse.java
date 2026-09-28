package Application.adapters.in.rest.responses;

/**
 * Response DTO: shipment status and tracking information.
 */
public record ShipmentResponse(String shipmentId, String orderId, String operatorId,
                               String originWarehouse, String shipmentStatus,
                               String dispatchDate, String deliveryDate) {
}
