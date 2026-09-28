package Application.domain.ports.in;

import Application.domain.models.Order;

/**
 * Input port (use case): registers the financial confirmation of an order
 * and, once its shipment has been delivered, finalizes it. A finalized
 * order cannot be modified afterward.
 */
public interface UpdateOrderStatusUseCase {

    /**
     * PENDING_PAYMENT -> PAID. Digital-only orders are delivered at once.
     */
    Order confirmPayment(String requesterId, String orderId);

    /**
     * SHIPPED -> DELIVERED, only once the shipment has been delivered.
     */
    Order finalizeOrder(String requesterId, String orderId);
}
