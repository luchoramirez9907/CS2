package Application.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Application.domain.exceptions.OrderAlreadyFinalizedException;
import Application.domain.valueobjects.OrderStatus;

/**
 * Order
 *
 * Represents the formal commercial commitment between a buyer and the
 * Marketplace. Its lifecycle is the central process of the system.
 *
 * Business rules: every order must be linked to an authenticated buyer;
 * each status transition represents a significant business event
 * (CART -> PENDING_PAYMENT -> PAID -> SHIPPED -> DELIVERED); a finalized
 * order cannot be modified under any circumstance.
 */
public class Order {

    private final String orderId;
    private final Buyer buyer;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus orderStatus;
    private final LocalDateTime creationDate;
    private BigDecimal totalAmount;
    private Invoice invoice;
    private Shipment shipment;
    private final List<Return> returns = new ArrayList<>();

    public Order(String orderId, Buyer buyer, LocalDateTime creationDate) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order id must not be null or blank");
        }
        if (buyer == null) {
            throw new IllegalArgumentException("Every order must be linked to an authenticated buyer");
        }
        if (creationDate == null) {
            throw new IllegalArgumentException("Order creation date must not be null");
        }
        this.orderId = orderId;
        this.buyer = buyer;
        this.creationDate = creationDate;
        this.orderStatus = OrderStatus.CART;
        this.totalAmount = BigDecimal.ZERO;
    }

    /** Builds an order from the confirmed selections of a shopping cart. */
    public static Order fromCart(ShoppingCart cart, String orderId, LocalDateTime creationDate) {
        if (cart == null) {
            throw new IllegalArgumentException("A ShoppingCart is required to create an Order");
        }
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an Order from an empty shopping cart");
        }
        Order order = new Order(orderId, cart.getBuyer(), creationDate);
        for (CartItem cartItem : cart.getItems()) {
            order.addItem(cartItem.getProduct(), cartItem.getQuantity(), cartItem.getUnitPrice());
        }
        return order;
    }

    public String getOrderId() { return orderId; }

    public Buyer getBuyer() { return buyer; }

    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }

    public OrderStatus getOrderStatus() { return orderStatus; }

    public LocalDateTime getCreationDate() { return creationDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public Invoice getInvoice() { return invoice; }

    public Shipment getShipment() { return shipment; }

    public List<Return> getReturns() { return Collections.unmodifiableList(returns); }

    private void addItem(Product product, int quantity, BigDecimal unitPrice) {
        items.add(new OrderItem(product, quantity, unitPrice));
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Ensures the order is still modifiable. A finalized (DELIVERED) order
     * cannot be modified under any circumstance.
     */
    public void ensureModifiable() {
        if (orderStatus == OrderStatus.DELIVERED) {
            throw new OrderAlreadyFinalizedException(orderId, orderStatus);
        }
    }

    /** Applies a lifecycle transition, validating the allowed order of events. */
    public void transitionTo(OrderStatus target) {
        ensureModifiable();
        if (target == null) {
            throw new IllegalArgumentException("Target status must not be null");
        }
        if (!orderStatus.canTransitionTo(target)) {
            throw new IllegalArgumentException("Illegal order transition from "
                    + orderStatus.getCode() + " to " + target.getCode());
        }
        this.orderStatus = target;
    }

    public void confirm() {
        transitionTo(OrderStatus.PENDING_PAYMENT);
    }

    public void markPaid() {
        transitionTo(OrderStatus.PAID);
    }

    public void markShipped() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void markDelivered() {
        transitionTo(OrderStatus.DELIVERED);
    }

    public void attachInvoice(Invoice invoice) {
        ensureModifiable();
        if (invoice == null || !this.equals(invoice.getOrder())) {
            throw new IllegalArgumentException("Invoice must belong to this order");
        }
        if (this.invoice != null) {
            throw new IllegalArgumentException("Order already has an invoice");
        }
        this.invoice = invoice;
    }

    public void attachShipment(Shipment shipment) {
        ensureModifiable();
        if (shipment == null || !this.equals(shipment.getOrder())) {
            throw new IllegalArgumentException("Shipment must fulfill this order");
        }
        if (this.shipment != null) {
            throw new IllegalArgumentException("Order already has a shipment");
        }
        this.shipment = shipment;
    }

    /**
     * Registers a return generated by this order. Returns are post-sale
     * records (the order is already delivered), so they do not modify the
     * order's commercial data.
     */
    public void addReturn(Return returnRequest) {
        if (returnRequest == null || !this.equals(returnRequest.getOrder())) {
            throw new IllegalArgumentException("Return must belong to this order");
        }
        this.returns.add(returnRequest);
    }

    /**
     * Rebuilds a persisted order without re-applying lifecycle
     * validations. Used exclusively by persistence mappers. The invoice
     * snapshot (when present) is restored as part of the order.
     */
    public static Order reconstruct(String orderId, Buyer buyer, List<OrderItem> items,
                                    OrderStatus orderStatus, LocalDateTime creationDate,
                                    InvoiceSnapshot invoiceSnapshot) {
        Order order = new Order(orderId, buyer, creationDate);
        order.items.addAll(items);
        order.recalculateTotal();
        order.orderStatus = orderStatus;
        if (invoiceSnapshot != null) {
            order.invoice = new Invoice(invoiceSnapshot.invoiceId(), order,
                    invoiceSnapshot.issueDate(), invoiceSnapshot.totalAmount(), invoiceSnapshot.taxAmount());
        }
        return order;
    }

    /**
     * Persisted invoice data of an order.
     */
    public record InvoiceSnapshot(String invoiceId, LocalDateTime issueDate,
                                  java.math.BigDecimal totalAmount, java.math.BigDecimal taxAmount) {
    }
}
