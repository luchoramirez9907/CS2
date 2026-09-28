package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.InvoiceResponse;
import Application.domain.models.Invoice;

import java.time.format.DateTimeFormatter;

/**
 * Mapper: Domain Model (Invoice) to Response DTO.
 */
public final class InvoiceDtoMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private InvoiceDtoMapper() {
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getInvoiceId(),
                invoice.getOrder().getOrderId(),
                invoice.getIssueDate().format(ISO),
                invoice.getTotalAmount(),
                invoice.getTaxAmount());
    }
}
