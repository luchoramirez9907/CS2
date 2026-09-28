package Application.domain.services;

import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.models.Inventory;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.ports.in.ConsultCommercialReportUseCase;
import Application.domain.ports.in.ConsultSellerPerformanceReportUseCase;
import Application.domain.ports.out.InventoryRepository;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.valueobjects.SystemRole;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportingService
 *
 * Implements the Administrative Reporting services (per SDD - Services):
 * - Consult Commercial Report: consolidated orders, invoices, revenue
 *   and inventory levels across warehouses (Administrator/Supervisor).
 * - Consult Seller Performance Report: consolidated products, orders and
 *   returns of a seller (Administrator, Supervisor or the seller itself).
 */
public class ReportingService implements ConsultCommercialReportUseCase, ConsultSellerPerformanceReportUseCase {

    private final PersonRepository personRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ReportingService(PersonRepository personRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository,
                            InventoryRepository inventoryRepository) {
        if (personRepository == null || orderRepository == null
                || productRepository == null || inventoryRepository == null) {
            throw new IllegalArgumentException("ReportingService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public CommercialReport consultCommercialReport(String requesterId) {
        requireText(requesterId, "requester id");
        requireReportingRole(requesterId, "consult the commercial report");

        List<Order> orders = orderRepository.findAll();
        Map<String, Integer> ordersByStatus = new LinkedHashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        int invoicedOrders = 0;
        for (Order order : orders) {
            ordersByStatus.merge(order.getOrderStatus().getCode(), 1, Integer::sum);
            if (order.getInvoice() != null) {
                invoicedOrders++;
                totalRevenue = totalRevenue.add(order.getInvoice().getTotalAmount());
                totalTax = totalTax.add(order.getInvoice().getTaxAmount());
            }
        }

        List<Inventory> inventories = inventoryRepository.findAll();
        int totalAvailable = inventories.stream().mapToInt(Inventory::getAvailableQuantity).sum();
        int totalReserved = inventories.stream().mapToInt(Inventory::getReservedQuantity).sum();

        return new CommercialReport(orders.size(), ordersByStatus, totalRevenue, totalTax,
                invoicedOrders, totalAvailable, totalReserved, inventories.size());
    }

    @Override
    public SellerPerformanceReport consultSellerPerformanceReport(String requesterId, String sellerId) {
        requireText(requesterId, "requester id");
        requireText(sellerId, "seller id");
        requireSellerReportAccess(requesterId, sellerId);

        List<Product> products = productRepository.findBySellerId(sellerId);
        Map<String, Integer> productsByStatus = new LinkedHashMap<>();
        for (Product product : products) {
            productsByStatus.merge(product.getStatus().getCode(), 1, Integer::sum);
        }

        List<Order> sellerOrders = orderRepository.findAll().stream()
                .filter(order -> order.getItems().stream()
                        .anyMatch(item -> item.getProduct().getSeller().getIdentifier().equals(sellerId)))
                .toList();
        int returnCount = sellerOrders.stream().mapToInt(order -> order.getReturns().size()).sum();

        return new SellerPerformanceReport(sellerId, products.size(), productsByStatus,
                sellerOrders.size(), returnCount);
    }

    private void requireReportingRole(String requesterId, String operation) {
        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();
        if (requester.getRole() != SystemRole.ADMINISTRATOR
                && requester.getRole() != SystemRole.SUPERVISOR) {
            throw new InvalidRoleAssignmentException(operation, requester.getRole(),
                    "an ADMINISTRATOR or a SUPERVISOR");
        }
    }

    private void requireSellerReportAccess(String requesterId, String sellerId) {
        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();
        if (!requester.getIdentifier().equals(sellerId)
                && requester.getRole() != SystemRole.ADMINISTRATOR
                && requester.getRole() != SystemRole.SUPERVISOR) {
            throw new InvalidRoleAssignmentException("consult a seller performance report",
                    requester.getRole(),
                    "the seller itself, an ADMINISTRATOR or a SUPERVISOR");
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
