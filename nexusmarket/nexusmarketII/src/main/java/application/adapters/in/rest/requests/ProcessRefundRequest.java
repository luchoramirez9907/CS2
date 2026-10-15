package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO: POST /api/v1/admin/returns/{returnId}/refund
 * amount: optional; when omitted the total of the order is refunded.
 */
public record ProcessRefundRequest(@Positive @Digits(integer = 13, fraction = 2) BigDecimal amount) {
}
