package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST .../orders/{orderId}/shipment
 * logisticsOperatorId is required for administrators; for logistics
 * operators the shipment is always assigned to the authenticated operator.
 */
public record CreateShipmentRequest(@NotBlank @Size(max = 64) String originWarehouseId,
                                    @Size(max = 64) String logisticsOperatorId) {
}
