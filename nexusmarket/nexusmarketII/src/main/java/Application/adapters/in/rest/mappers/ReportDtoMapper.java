package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.CommercialReportResponse;
import Application.adapters.in.rest.responses.SellerPerformanceReportResponse;
import Application.domain.models.CommercialReport;
import Application.domain.models.SellerPerformanceReport;

import java.time.format.DateTimeFormatter;

/**
 * Mapper: Domain report models to Response DTOs.
 */
public final class ReportDtoMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ReportDtoMapper() {
    }

    public static CommercialReportResponse toResponse(CommercialReport report) {
        return new CommercialReportResponse(
                report.generatedAt().format(ISO),
                report.totalOrders(),
                report.ordersByStatus(),
                report.invoicesIssued(),
                report.invoicedAmount(),
                report.invoicedTaxAmount(),
                report.grossRevenue(),
                report.refundedAmount(),
                report.netRevenue(),
                report.inventoryLevels().stream()
                        .map(level -> new CommercialReportResponse.InventoryLevelResponse(
                                level.warehouseId(), level.warehouseName(), level.productId(),
                                level.productName(), level.availableQuantity(),
                                level.reservedQuantity(), level.damagedQuantity()))
                        .toList());
    }

    public static SellerPerformanceReportResponse toResponse(SellerPerformanceReport report) {
        return new SellerPerformanceReportResponse(
                report.sellerId(),
                report.sellerName(),
                report.generatedAt().format(ISO),
                report.totalProducts(),
                report.productsByStatus(),
                report.ordersCount(),
                report.unitsSold(),
                report.salesAmount(),
                report.returnsCount(),
                report.returnsByStatus());
    }
}
