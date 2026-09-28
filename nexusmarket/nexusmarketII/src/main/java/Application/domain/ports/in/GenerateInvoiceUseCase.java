package Application.domain.ports.in;

import Application.domain.models.Invoice;

/**
 * Input port (use case): creates the billing record of a confirmed order,
 * including the applicable total and tax amounts.
 */
public interface GenerateInvoiceUseCase {

    Invoice generateInvoice(String requesterId, String orderId);
}
