package Application.adapters.in.rest.requests;

import java.math.BigDecimal;

/**
 * Request DTO: generate the invoice of a confirmed order.
 */
public record GenerateInvoiceRequest(String performerId, BigDecimal taxRate) {
}
