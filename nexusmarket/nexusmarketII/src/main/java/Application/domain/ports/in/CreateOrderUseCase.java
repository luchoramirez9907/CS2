package Application.domain.ports.in;

import Application.domain.models.Order;

/**
 * Input port (use case): converts the buyer's shopping cart into a
 * formal commercial commitment (Order), generating its invoice.
 */
public interface CreateOrderUseCase {

    /**
     * @param buyerId identifier of the Buyer placing the order
     * @param cartId  identifier of the cart to convert (optional; resolved when blank)
     * @return the confirmed Order (status PENDING_PAYMENT)
     */
    Order checkout(String buyerId, String cartId);
}
