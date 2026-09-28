package Application.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * SellerPerformanceReport
 *
 * Consolidated view of the products, orders and returns associated with
 * a seller.
 *
 * @param productsByStatus  number of products per ProductStatus code
 * @param ordersCount       orders containing at least one product of the seller
 * @param unitsSold         units of the seller's products in paid orders
 * @param salesAmount       amount of the seller's lines in paid orders
 * @param returnsByStatus   number of returns per ReturnStatus over those orders
 */
public record SellerPerformanceReport(String sellerId,
                                      String sellerName,
                                      LocalDateTime generatedAt,
                                      int totalProducts,
                                      Map<String, Long> productsByStatus,
                                      int ordersCount,
                                      int unitsSold,
                                      BigDecimal salesAmount,
                                      int returnsCount,
                                      Map<String, Long> returnsByStatus) {
}
