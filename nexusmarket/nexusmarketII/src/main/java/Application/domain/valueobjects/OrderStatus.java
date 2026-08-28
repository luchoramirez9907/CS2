package Application.domain.valueobjects;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OrderStatus
 *
 * Represents the current state of an order as it progresses through its
 * commercial and logistics lifecycle.
 *
 * Lifecycle: CART -> PENDING_PAYMENT -> PAID -> SHIPPED -> DELIVERED
 */
public final class OrderStatus extends DomainCatalog {

    public static final OrderStatus CART =
            new OrderStatus("CART", "Cart",
                    "Products provisionally selected, order not yet confirmed.", null);
    public static final OrderStatus PENDING_PAYMENT =
            new OrderStatus("PENDING_PAYMENT", "Pending Payment",
                    "Order confirmed and awaiting financial confirmation.", CART);
    public static final OrderStatus PAID =
            new OrderStatus("PAID", "Paid",
                    "Payment confirmed; preparation process may begin.", PENDING_PAYMENT);
    public static final OrderStatus SHIPPED =
            new OrderStatus("SHIPPED", "Shipped",
                    "Order has physically left the warehouse.", PAID);
    public static final OrderStatus DELIVERED =
            new OrderStatus("DELIVERED", "Delivered",
                    "Order has been successfully delivered to the buyer.", SHIPPED);

    private static final Map<String, OrderStatus> VALUES_BY_CODE;

    static {
        Map<String, OrderStatus> values = new LinkedHashMap<>();
        values.put(CART.code(), CART);
        values.put(PENDING_PAYMENT.code(), PENDING_PAYMENT);
        values.put(PAID.code(), PAID);
        values.put(SHIPPED.code(), SHIPPED);
        values.put(DELIVERED.code(), DELIVERED);
        VALUES_BY_CODE = Collections.unmodifiableMap(values);
    }

    private final OrderStatus previousStatus;

    private OrderStatus(String code, String name, String description, OrderStatus previousStatus) {
        super(code, name, description);
        this.previousStatus = previousStatus;
    }

    /**
     * Determines whether the order lifecycle allows a direct transition
     * from this status to the given target status.
     */
    public boolean canTransitionTo(OrderStatus target) {
        return target != null && target.previousStatus == this;
    }

    public static OrderStatus fromCode(String code) {
        OrderStatus status = VALUES_BY_CODE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
        }
        return status;
    }

    public static Collection<OrderStatus> values() {
        return VALUES_BY_CODE.values();
    }

    private String code() {
        return getCode();
    }
}
