package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.enums.RefundStatus;
import Application.domain.models.CommercialReport;
import Application.domain.models.Inventory;
import Application.domain.models.Order;
import Application.domain.models.OrderItem;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.Refund;
import Application.domain.models.Return;
import Application.domain.models.Seller;
import Application.domain.models.SellerPerformanceReport;
import Application.domain.ports.in.ConsultCommercialReportUseCase;
import Application.domain.ports.in.ConsultSellerPerformanceReportUseCase;
import Application.domain.ports.out.InventoryRepository;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.ReturnRepository;
import Application.domain.ports.out.SellerRepository;
import Application.domain.valueobjects.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ReportingService
 *
 * Implements ConsultCommercialReportUseCase and
 * ConsultSellerPerformanceReportUseCase: read-only consolidated views for
 * administrative review. Revenue only counts orders whose payment was
 * confirmed (PAID, SHIPPED or DELIVERED).
 */
public class ReportingService implements ConsultCommercialReportUseCase, ConsultSellerPerformanceReportUseCase {

    private static final Set<OrderStatus> PAID_STATUSES =
            Set.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final AuthorizationService authorizationService;

    public ReportingService(OrderRepository orderRepository,
                            ReturnRepository returnRepository,
                            InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            SellerRepository sellerRepository,
                            AuthorizationService authorizationService) {
        if (orderRepository == null || returnRepository == null || inventoryRepository == null
                || productRepository == null || sellerRepository == null || authorizationService == null) {
            throw new IllegalArgumentException("ReportingService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public CommercialReport consultCommercialReport(String requesterId) {
        authorizationService.requirePermission(requesterId, BusinessOperation.CONSULT_COMMERCIAL_REPORT);

        List<Order> orders = orderRepository.findAll();
        Map<String, Long> ordersByStatus = countBy(orders, order -> order.getOrderStatus().getCode());

        List<Order> invoiced = orders.stream().filter(order -> order.getInvoice() != null).toList();
        BigDecimal invoicedAmount = sum(invoiced, order -> order.getInvoice().getTotalAmount());
        BigDecimal invoicedTax = sum(invoiced, order -> order.getInvoice().getTaxAmount());

        BigDecimal grossRevenue = sum(orders.stream().filter(this::isPaid).toList(), Order::getTotalAmount);
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

    @Override
    public SellerPerformanceReport consultSellerPerformanceReport(String requesterId, String sellerId) {
        Person requester = authorizationService.requirePermission(requesterId,
                BusinessOperation.CONSULT_SELLER_PERFORMANCE_REPORT);
        ServiceValidations.requireText(sellerId, "seller id");
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller '" + sellerId + "' does not exist"));
        authorizationService.requireSellerAccess(requester, seller);

        List<Product> products = productRepository.findBySellerId(sellerId);
        Map<String, Long> productsByStatus = countBy(products, product -> product.getStatus().getCode());

        List<Order> sellerOrders = orderRepository.findAll().stream()
                .filter(order -> order.containsProductsOf(sellerId))
                .toList();
        List<OrderItem> soldItems = sellerOrders.stream()
                .filter(this::isPaid)
                .flatMap(order -> order.getItems().stream())
                .filter(item -> item.getProduct().getSeller().getIdentifier().equals(sellerId))
                .toList();
        int unitsSold = soldItems.stream().mapToInt(OrderItem::getQuantity).sum();
        BigDecimal salesAmount = sum(soldItems, OrderItem::getSubtotal);

        Set<String> sellerOrderIds = sellerOrders.stream().map(Order::getOrderId).collect(Collectors.toSet());
        List<Return> returns = returnRepository.findAll().stream()
                .filter(returnRequest -> sellerOrderIds.contains(returnRequest.getOrder().getOrderId()))
                .toList();
        Map<String, Long> returnsByStatus = countBy(returns, returnRequest -> returnRequest.getReturnStatus().name());

        return new SellerPerformanceReport(seller.getIdentifier(), seller.getFullName(), LocalDateTime.now(),
                products.size(), productsByStatus, sellerOrders.size(), unitsSold, salesAmount,
                returns.size(), returnsByStatus);
    }

    private boolean isPaid(Order order) {
        return PAID_STATUSES.contains(order.getOrderStatus());
    }

    private static <T> Map<String, Long> countBy(List<T> values, Function<T, String> classifier) {
        return values.stream().collect(Collectors.groupingBy(classifier, LinkedHashMap::new,
                Collectors.counting()));
    }

    private static <T> BigDecimal sum(List<T> values, Function<T, BigDecimal> amount) {
        return values.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
