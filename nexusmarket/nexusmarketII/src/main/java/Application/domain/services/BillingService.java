package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Invoice;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.ports.in.ConsultInvoiceUseCase;
import Application.domain.ports.in.GenerateInvoiceUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.valueobjects.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BillingService
 *
 * Implements GenerateInvoiceUseCase and ConsultInvoiceUseCase. An Invoice
 * belongs to exactly one confirmed Order (never to a cart) and is issued
 * only once. Invoices are generated automatically at checkout; the use
 * case allows an Administrator to issue the invoice of a confirmed order
 * that does not have one yet.
 */
public class BillingService implements GenerateInvoiceUseCase, ConsultInvoiceUseCase {

    /**
     * Tax rate applied to issued invoices (business-configurable).
     */
    public static final BigDecimal DEFAULT_TAX_RATE = BigDecimal.ZERO;

    private final OrderRepository orderRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;
    private final BigDecimal taxRate;

    public BillingService(OrderRepository orderRepository,
                          AuthorizationService authorizationService,
                          NotificationService notificationService) {
        this(orderRepository, authorizationService, notificationService, DEFAULT_TAX_RATE);
    }

    public BillingService(OrderRepository orderRepository,
                          AuthorizationService authorizationService,
                          NotificationService notificationService,
                          BigDecimal taxRate) {
        if (orderRepository == null || authorizationService == null || notificationService == null) {
            throw new IllegalArgumentException("BillingService requires its dependencies");
        }
        if (taxRate == null || taxRate.signum() < 0) {
            throw new IllegalArgumentException("Tax rate must not be null or negative");
        }
        this.orderRepository = orderRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
        this.taxRate = taxRate;
    }

    /**
     * Issues and attaches the invoice of a confirmed order (domain
     * operation shared with the checkout process; does not persist).
     */
    public Invoice issueInvoice(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        if (order.getOrderStatus() == OrderStatus.CART) {
            throw new IllegalArgumentException("An invoice can only be generated for a confirmed order");
        }
        Invoice invoice = Invoice.issueFor(order, UUID.randomUUID().toString(), LocalDateTime.now(), taxRate);
        order.attachInvoice(invoice);
        return invoice;
    }

    @Override
    public Invoice generateInvoice(String requesterId, String orderId) {
        authorizationService.requirePermission(requesterId, BusinessOperation.GENERATE_INVOICE);
        Order order = findOrder(orderId);
        if (order.getInvoice() != null) {
            throw new IllegalArgumentException("Order '" + orderId + "' already has an invoice");
        }
        Invoice invoice = issueInvoice(order);
        orderRepository.save(order);
        notificationService.notify(order.getBuyer(), "Invoice issued",
                "Invoice " + invoice.getInvoiceId() + " was issued for order " + orderId);
        return invoice;
    }

    @Override
    public Invoice consultInvoice(String requesterId, String orderId) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.CONSULT_INVOICE);
        Order order = findOrder(orderId);
        authorizationService.requireOrderAccess(requester, order);
        if (order.getInvoice() == null) {
            throw new IllegalArgumentException("Order '" + orderId + "' has no invoice");
        }
        return order.getInvoice();
    }

    private Order findOrder(String orderId) {
        ServiceValidations.requireText(orderId, "order id");
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order '" + orderId + "' does not exist"));
    }
}
