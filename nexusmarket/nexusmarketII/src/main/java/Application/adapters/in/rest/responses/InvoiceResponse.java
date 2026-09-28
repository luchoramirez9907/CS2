package Application.adapters.in.rest.responses;

import java.math.BigDecimal;

/**
 * Response DTO: invoice of an order.
 */
public record InvoiceResponse(String invoiceId, String orderId, String issueDate,
                              BigDecimal totalAmount, BigDecimal taxAmount) {
}
