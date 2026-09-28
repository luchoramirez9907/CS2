package Application.domain.ports.in;

import java.util.Map;

/**
 * Input port (use case): Consult Seller Performance Report.
 *
 * Retrieves consolidated information about products, orders and returns
 * associated with a seller.
 */
public interface ConsultSellerPerformanceReportUseCase {

    record SellerPerformanceReport(String sellerId,
                                   int productCount,
                                   Map<String, Integer> productsByStatus,
                                   int orderCount,
                                   int returnCount) {
    }

    SellerPerformanceReport consultSellerPerformanceReport(String requesterId, String sellerId);
}
