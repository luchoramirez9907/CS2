package Application.domain.ports.in;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Input port (use case): Consult Commercial Report.
 *
 * Retrieves consolidated information about orders, invoices, revenue
 * and inventory levels across warehouses for administrative review.
 */
public interface ConsultCommercialReportUseCase {

    record CommercialReport(int totalOrders,
                            Map<String, Integer> ordersByStatus,
                            BigDecimal totalRevenue,
                            BigDecimal totalTaxAmount,
                            int invoicedOrders,
                            int totalAvailableStock,
                            int totalReservedStock,
                            int inventoryRecords) {
    }

    CommercialReport consultCommercialReport(String requesterId);
}
