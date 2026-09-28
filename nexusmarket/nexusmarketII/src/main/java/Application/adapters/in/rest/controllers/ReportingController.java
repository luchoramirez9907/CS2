package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.CommercialReportResponse;
import Application.adapters.in.rest.responses.SellerPerformanceResponse;
import Application.domain.ports.in.ConsultCommercialReportUseCase;
import Application.domain.ports.in.ConsultSellerPerformanceReportUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Administrative Reporting.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ConsultCommercialReportUseCase consultCommercialReportUseCase;
    private final ConsultSellerPerformanceReportUseCase consultSellerPerformanceReportUseCase;

    public ReportingController(ConsultCommercialReportUseCase consultCommercialReportUseCase,
                               ConsultSellerPerformanceReportUseCase consultSellerPerformanceReportUseCase) {
        this.consultCommercialReportUseCase = consultCommercialReportUseCase;
        this.consultSellerPerformanceReportUseCase = consultSellerPerformanceReportUseCase;
    }

    @GetMapping("/commercial")
    public ResponseEntity<ApiResponse<CommercialReportResponse>> commercial(
            @RequestParam String requesterId) {
        ConsultCommercialReportUseCase.CommercialReport report =
                consultCommercialReportUseCase.consultCommercialReport(requesterId);
        return ResponseEntity.ok(ApiResponse.ok("Commercial report",
                new CommercialReportResponse(report.totalOrders(), report.ordersByStatus(),
                        report.totalRevenue(), report.totalTaxAmount(), report.invoicedOrders(),
                        report.totalAvailableStock(), report.totalReservedStock(),
                        report.inventoryRecords())));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<SellerPerformanceResponse>> sellerPerformance(
            @PathVariable String sellerId,
            @RequestParam String requesterId) {
        ConsultSellerPerformanceReportUseCase.SellerPerformanceReport report =
                consultSellerPerformanceReportUseCase.consultSellerPerformanceReport(
                        requesterId, sellerId);
        return ResponseEntity.ok(ApiResponse.ok("Seller performance report",
                new SellerPerformanceResponse(report.sellerId(), report.productCount(),
                        report.productsByStatus(), report.orderCount(), report.returnCount())));
    }
}
