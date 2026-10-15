package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST .../orders/{orderId}/shipment/return (failed delivery).
 */
public record ShipmentReturnRequest(@NotBlank @Size(max = 500) String reason) {
}
