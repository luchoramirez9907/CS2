package Application.adapters.in.rest.responses;

import java.util.Map;

/**
 * Response DTO: seller performance report.
 */
public record SellerPerformanceResponse(String sellerId, int productCount,
                                        Map<String, Integer> productsByStatus,
                                        int orderCount, int returnCount) {
}
