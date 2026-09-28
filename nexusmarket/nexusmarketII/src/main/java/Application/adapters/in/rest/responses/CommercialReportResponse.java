package Application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Response DTO: administrative commercial report.
 */
public record CommercialReportResponse(String generatedAt, int totalOrders,
                                       Map<String, Long> ordersByStatus, int invoicesIssued,
                                       BigDecimal invoicedAmount, BigDecimal invoicedTaxAmount,
                                       BigDecimal grossRevenue, BigDecimal refundedAmount,
                                       BigDecimal netRevenue, List<InventoryLevelResponse> inventoryLevels) {

    public record InventoryLevelResponse(String warehouseId, String warehouseName, String productId,
                                         String productName, int availableQuantity,
                                         int reservedQuantity, int damagedQuantity) {
    }
}
