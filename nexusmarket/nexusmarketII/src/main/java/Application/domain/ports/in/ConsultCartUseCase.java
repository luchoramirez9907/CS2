package Application.domain.ports.in;

import Application.domain.models.ShoppingCart;

import java.util.Optional;

/**
 * Input port (use case): retrieves the current contents of a buyer's
 * shopping cart.
 */
public interface ConsultCartUseCase {

    /**
     * @return the active cart, or empty when the buyer has no active cart
     */
    Optional<ShoppingCart> consultCart(String buyerId);
}
