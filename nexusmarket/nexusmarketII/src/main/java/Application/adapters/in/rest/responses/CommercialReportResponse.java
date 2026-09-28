package Application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO: consolidated commercial report.
 */
public record CommercialReportResponse(int totalOrders,
                                       Map<String, Integer> ordersByStatus,
                                       BigDecimal totalRevenue,
                                       BigDecimal totalTaxAmount,
                                       int invoicedOrders,
                                       int totalAvailableStock,
                                       int totalReservedStock,
                                       int inventoryRecords) {
}
