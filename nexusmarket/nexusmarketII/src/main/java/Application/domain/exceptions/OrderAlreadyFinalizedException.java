package Application.domain.exceptions;

import Application.domain.valueobjects.OrderStatus;

/**
 * Thrown when an operation attempts to modify a finalized order.
 * A finalized order cannot be modified under any circumstance (per SDD).
 */
public class OrderAlreadyFinalizedException extends DomainException {

    private final String orderId;
    private final OrderStatus currentStatus;

    public OrderAlreadyFinalizedException(String orderId, OrderStatus currentStatus) {
        super("Order '" + orderId + "' is finalized with status " + currentStatus.getCode()
                + " and cannot be modified");
        this.orderId = orderId;
        this.currentStatus = currentStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
}
