package Application.adapters.in.rest.requests;

import java.math.BigDecimal;

/**
 * Request DTO: POST /api/refunds
 */
public record ProcessRefundRequest(String approverId, String returnId, BigDecimal amount) {
}
