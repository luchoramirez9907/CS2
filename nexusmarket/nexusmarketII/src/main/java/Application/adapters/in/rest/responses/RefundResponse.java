package Application.adapters.in.rest.responses;

import java.math.BigDecimal;

/**
 * Response DTO: refund information.
 */
public record RefundResponse(String refundId, String returnId, BigDecimal amount,
                             String refundStatus, String approvedBy) {
}
