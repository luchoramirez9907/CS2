package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.ReportDtoMapper;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.CommercialReportResponse;
import Application.adapters.in.rest.responses.SellerPerformanceReportResponse;
import Application.domain.ports.in.ConsultCommercialReportUseCase;
import Application.domain.ports.in.ConsultSellerPerformanceReportUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for administrative reporting. The requesting user is
 * identified by the X-User-Id header.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ConsultCommercialReportUseCase commercialReportUseCase;
    private final ConsultSellerPerformanceReportUseCase sellerPerformanceReportUseCase;

    public ReportController(ConsultCommercialReportUseCase commercialReportUseCase,
                            ConsultSellerPerformanceReportUseCase sellerPerformanceReportUseCase) {
        this.commercialReportUseCase = commercialReportUseCase;
        this.sellerPerformanceReportUseCase = sellerPerformanceReportUseCase;
    }

    @GetMapping("/commercial")
    public ResponseEntity<ApiResponse<CommercialReportResponse>> commercial(
            @RequestHeader("X-User-Id") String requesterId) {
        return ResponseEntity.ok(ApiResponse.ok("Commercial report",
                ReportDtoMapper.toResponse(commercialReportUseCase.consultCommercialReport(requesterId))));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<SellerPerformanceReportResponse>> sellerPerformance(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String sellerId) {
        return ResponseEntity.ok(ApiResponse.ok("Seller performance report",
                ReportDtoMapper.toResponse(
                        sellerPerformanceReportUseCase.consultSellerPerformanceReport(requesterId, sellerId))));
    }
}
