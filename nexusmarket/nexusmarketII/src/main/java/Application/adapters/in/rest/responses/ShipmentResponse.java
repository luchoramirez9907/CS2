package Application.adapters.in.rest.responses;

/**
 * Response DTO: shipment information.
 */
public record ShipmentResponse(String shipmentId, String orderId, String shipmentStatus,
                               String logisticsOperatorId, String originWarehouseId,
                               AddressResponse shippingAddress, String dispatchDate,
                               String deliveryDate) {
}
