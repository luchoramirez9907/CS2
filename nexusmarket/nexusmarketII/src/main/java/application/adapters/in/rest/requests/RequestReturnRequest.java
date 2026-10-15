package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST /api/v1/buyer/orders/{orderId}/returns
 */
public record RequestReturnRequest(@NotBlank @Size(max = 500) String reason) {
}
