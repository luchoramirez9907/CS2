package Application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO: seller performance report.
 */
public record SellerPerformanceReportResponse(String sellerId, String sellerName, String generatedAt,
                                              int totalProducts, Map<String, Long> productsByStatus,
                                              int ordersCount, int unitsSold, BigDecimal salesAmount,
                                              int returnsCount, Map<String, Long> returnsByStatus) {
}
