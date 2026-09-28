package Application.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * CommercialReport
 *
 * Consolidated administrative view of orders, invoices, revenue and
 * inventory levels across warehouses.
 *
 * @param ordersByStatus   number of orders per OrderStatus code
 * @param grossRevenue     total of the orders whose payment was confirmed
 * @param refundedAmount   total of the processed refunds
 * @param netRevenue       grossRevenue minus refundedAmount
 * @param inventoryLevels  stock of every product at every warehouse
 */
public record CommercialReport(LocalDateTime generatedAt,
                               int totalOrders,
                               Map<String, Long> ordersByStatus,
                               int invoicesIssued,
                               BigDecimal invoicedAmount,
                               BigDecimal invoicedTaxAmount,
                               BigDecimal grossRevenue,
                               BigDecimal refundedAmount,
                               BigDecimal netRevenue,
                               List<InventoryLevel> inventoryLevels) {

    public record InventoryLevel(String warehouseId, String warehouseName,
                                 String productId, String productName,
                                 int availableQuantity, int reservedQuantity,
                                 int damagedQuantity) {
    }
}
