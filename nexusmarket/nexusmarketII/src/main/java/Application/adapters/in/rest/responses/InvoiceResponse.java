package Application.adapters.in.rest.responses;

import java.math.BigDecimal;

/**
 * Response DTO: invoice information.
 */
public record InvoiceResponse(String invoiceId, String orderId, String issueDate,
                              BigDecimal totalAmount, BigDecimal taxAmount) {
}
