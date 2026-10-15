package application.domain.services.billing;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.OrderRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * GenerateInvoiceService
 *
 * Catalog service "Generate Invoice". An Invoice
 * belongs to exactly one confirmed Order (never to a cart) and is issued
 * only once, with the VAT contained in the catalog prices. Invoices are
 * issued automatically at checkout (issueInvoice, used by
 * ConfirmOrderService); the use case allows an Administrator to issue the
 * invoice of a confirmed order that does not have one yet.
 */
public class GenerateInvoiceService {

    /**
     * Default tax rate (VAT 19%) contained in the catalog prices. It is
     * business-configurable through the four-argument constructor.
     */
    public static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.19");

    private final OrderRepository orderRepository;
    private final ValidatePermissionsService permissionsService;
    private final NotificationService notificationService;
    private final BigDecimal taxRate;

    public GenerateInvoiceService(OrderRepository orderRepository,
                                  ValidatePermissionsService permissionsService,
                                  NotificationService notificationService) {
        this(orderRepository, permissionsService, notificationService, DEFAULT_TAX_RATE);
    }

    public GenerateInvoiceService(OrderRepository orderRepository,
                                  ValidatePermissionsService permissionsService,
                                  NotificationService notificationService,
                                  BigDecimal taxRate) {
        if (orderRepository == null || permissionsService == null || notificationService == null) {
            throw new IllegalArgumentException("GenerateInvoiceService requires its dependencies");
        }
        if (taxRate == null || taxRate.signum() < 0) {
            throw new IllegalArgumentException("Tax rate must not be null or negative");
        }
        this.orderRepository = orderRepository;
        this.permissionsService = permissionsService;
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

    public Invoice generateInvoice(String requesterId, String orderId) {
        permissionsService.requirePermission(requesterId, BusinessOperation.GENERATE_INVOICE);
        ServiceValidations.requireText(orderId, "order id");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        if (order.getInvoice() != null) {
            throw new IllegalArgumentException("Order '" + orderId + "' already has an invoice");
        }
        Invoice invoice = issueInvoice(order);
        orderRepository.save(order);
        notificationService.notify(order.getBuyer(), "Invoice issued",
                "Invoice " + invoice.getInvoiceId() + " was issued for order " + orderId);
        return invoice;
    }
}
