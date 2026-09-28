package Application.domain.ports.in;

import Application.domain.models.Order;

/**
 * Input port (use case): Update Order Status.
 *
 * Registers the financial confirmation of an order. Once the associated
 * shipment has been delivered, the order lifecycle is finalized by the
 * logistics process; a finalized order cannot be modified afterwards.
 */
public interface UpdateOrderStatusUseCase {

    /**
     * Transitions a PENDING_PAYMENT order to PAID.
     *
     * @param performerId identifier of the buyer who placed the order or an Administrator
     * @param orderId     identifier of the order to confirm financially
     * @return the updated Order
     */
    Order confirmPayment(String performerId, String orderId);
}
