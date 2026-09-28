package Application.domain.ports.in;

import Application.domain.models.Order;

/**
 * Input port (use case): retrieves an order according to the access
 * permissions of the requesting user.
 */
public interface ConsultOrderUseCase {

    Order consultOrder(String requesterId, String orderId);
}
