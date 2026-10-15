package application.domain.services.reporting;

import application.domain.enums.BusinessOperation;
import application.domain.enums.RefundStatus;
import application.domain.models.CommercialReport;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ReturnRepository;
import application.domain.services.authorization.ValidatePermissionsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static application.domain.services.reporting.ReportCalculations.countBy;
import static application.domain.services.reporting.ReportCalculations.sum;

/**
 * ConsultCommercialReportService
 *
 * Catalog service "Consult Commercial Report":
 * read-only consolidated view of orders,
 * invoices, revenue, refunds and inventory levels for administrative
 * review. Revenue only counts paid orders; refunds only count PROCESSED
 * refunds.
 */
public class ConsultCommercialReportService {

    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final InventoryRepository inventoryRepository;
    private final ValidatePermissionsService permissionsService;

    public ConsultCommercialReportService(OrderRepository orderRepository,
                                          ReturnRepository returnRepository,
                                          InventoryRepository inventoryRepository,
                                          ValidatePermissionsService permissionsService) {
        if (orderRepository == null || returnRepository == null || inventoryRepository == null
                || permissionsService == null) {
            throw new IllegalArgumentException("ConsultCommercialReportService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.inventoryRepository = inventoryRepository;
        this.permissionsService = permissionsService;
    }

    public CommercialReport consultCommercialReport(String requesterId) {
        permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_COMMERCIAL_REPORT);

        List<Order> orders = orderRepository.findAll();
        Map<String, Long> ordersByStatus = countBy(orders, order -> order.getOrderStatus().getCode());

        List<Order> invoiced = orders.stream().filter(order -> order.getInvoice() != null).toList();
        BigDecimal invoicedAmount = sum(invoiced, order -> order.getInvoice().getTotalAmount());
        BigDecimal invoicedTax = sum(invoiced, order -> order.getInvoice().getTaxAmount());

        BigDecimal grossRevenue = sum(orders.stream().filter(ReportCalculations::isPaid).toList(),
                Order::getTotalAmount);
        List<Refund> processedRefunds = returnRepository.findAll().stream()
                .map(Return::getRefund)
                .filter(Objects::nonNull)
                .filter(refund -> refund.getRefundStatus() == RefundStatus.PROCESSED)
                .toList();
        BigDecimal refundedAmount = sum(processedRefunds, Refund::getAmount);

        List<CommercialReport.InventoryLevel> inventoryLevels = inventoryRepository.findAll().stream()
                .sorted(Comparator.comparing((Inventory inventory) -> inventory.getWarehouse().getIdentifier())
                        .thenComparing(inventory -> inventory.getProduct().getIdentifier()))
                .map(inventory -> new CommercialReport.InventoryLevel(
                        inventory.getWarehouse().getIdentifier(),
                        inventory.getWarehouse().getName(),
                        inventory.getProduct().getIdentifier(),
                        inventory.getProduct().getName(),
                        inventory.getAvailableQuantity(),
                        inventory.getReservedQuantity(),
                        inventory.getDamagedQuantity()))
                .toList();

        return new CommercialReport(LocalDateTime.now(), orders.size(), ordersByStatus,
                invoiced.size(), invoicedAmount, invoicedTax, grossRevenue, refundedAmount,
                grossRevenue.subtract(refundedAmount), inventoryLevels);
    }
}
