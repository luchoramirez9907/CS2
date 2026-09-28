package Application.domain.ports.in;

import Application.domain.models.Invoice;

import java.math.BigDecimal;

/**
 * Input port (use case): Generate Invoice.
 *
 * Creates the billing record associated with a confirmed order,
 * including the applicable total and tax amounts.
 */
public interface GenerateInvoiceUseCase {

    /**
     * @param performerId identifier of the Administrator generating the invoice
     * @param orderId     identifier of the confirmed order
     * @param taxRate     applicable tax rate (e.g. 0.19 for 19%)
     * @return the generated Invoice
     */
    Invoice generateInvoice(String performerId, String orderId, BigDecimal taxRate);
}
