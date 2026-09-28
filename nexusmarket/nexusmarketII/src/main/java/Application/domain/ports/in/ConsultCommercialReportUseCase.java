package Application.domain.ports.in;

import Application.domain.models.CommercialReport;

/**
 * Input port (use case): consolidated orders, invoices, revenue and
 * inventory levels for administrative review.
 */
public interface ConsultCommercialReportUseCase {

    CommercialReport consultCommercialReport(String requesterId);
}
