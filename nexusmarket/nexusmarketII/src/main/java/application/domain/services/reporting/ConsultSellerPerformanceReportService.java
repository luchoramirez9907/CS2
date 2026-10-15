package application.domain.services.reporting;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.models.SellerPerformanceReport;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.ReturnRepository;
import application.domain.ports.out.SellerRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static application.domain.services.reporting.ReportCalculations.countBy;
import static application.domain.services.reporting.ReportCalculations.sum;

/**
 * ConsultSellerPerformanceReportService
 *
 * Catalog service "Consult Seller Performance Report":
 * read-only consolidated view of the
 * products, orders, sales and returns associated with a seller. A seller
 * only consults its own report. Sales only count paid orders.
 */
public class ConsultSellerPerformanceReportService {

    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public ConsultSellerPerformanceReportService(OrderRepository orderRepository,
                                                 ReturnRepository returnRepository,
                                                 ProductRepository productRepository,
                                                 SellerRepository sellerRepository,
                                                 ValidatePermissionsService permissionsService,
                                                 ValidateOwnershipAccessService ownershipService) {
        if (orderRepository == null || returnRepository == null || productRepository == null
                || sellerRepository == null || permissionsService == null || ownershipService == null) {
            throw new IllegalArgumentException("ConsultSellerPerformanceReportService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    public SellerPerformanceReport consultSellerPerformanceReport(String requesterId, String sellerId) {
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.CONSULT_SELLER_PERFORMANCE_REPORT);
        ServiceValidations.requireText(sellerId, "seller id");
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
        ownershipService.requireSellerAccess(requester, seller);

        List<Product> products = productRepository.findBySellerId(sellerId);
        Map<String, Long> productsByStatus = countBy(products, product -> product.getStatus().getCode());

        List<Order> sellerOrders = orderRepository.findAll().stream()
                .filter(order -> order.containsProductsOf(sellerId))
                .toList();
        List<OrderItem> soldItems = sellerOrders.stream()
                .filter(ReportCalculations::isPaid)
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
}
