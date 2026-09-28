package Application.domain.ports.in;

import Application.domain.models.ShoppingCart;

/**
 * Input port (use case): Consult Cart.
 *
 * Retrieves the current contents of a buyer's shopping cart.
 */
public interface ConsultCartUseCase {

    /**
     * @param buyerId identifier of the cart's owner
     * @param cartId  optional cart identifier; when blank, the buyer's
     *                active cart is returned
     */
    ShoppingCart consultCart(String buyerId, String cartId);
}
