package Application.domain.ports.in;

import Application.domain.models.SellerPerformanceReport;

/**
 * Input port (use case): consolidated products, orders and returns of a
 * seller.
 */
public interface ConsultSellerPerformanceReportUseCase {

    SellerPerformanceReport consultSellerPerformanceReport(String requesterId, String sellerId);
}
