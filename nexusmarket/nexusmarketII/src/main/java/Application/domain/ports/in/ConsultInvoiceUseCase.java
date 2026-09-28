package Application.domain.ports.in;

import Application.domain.models.Invoice;

/**
 * Input port (use case): retrieves the invoice of an order according to
 * the access permissions of the requesting user.
 */
public interface ConsultInvoiceUseCase {

    Invoice consultInvoice(String requesterId, String orderId);
}
