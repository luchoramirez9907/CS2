package Application.domain.ports.in;

import Application.domain.models.Invoice;

/**
 * Input port (use case): Consult Invoice.
 *
 * Retrieves the invoice information associated with an order according
 * to the access permissions of the requesting user.
 */
public interface ConsultInvoiceUseCase {

    Invoice consultInvoice(String requesterId, String orderId);
}
