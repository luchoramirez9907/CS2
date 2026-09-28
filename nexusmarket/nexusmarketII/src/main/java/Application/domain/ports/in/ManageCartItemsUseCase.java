package Application.domain.ports.in;

import Application.domain.models.ShoppingCart;

/**
 * Input port (use case): adds, updates the quantity of, removes or clears
 * products within a buyer's shopping cart.
 */
public interface ManageCartItemsUseCase extends AddItemToCartUseCase {

    ShoppingCart updateItemQuantity(String buyerId, String productId, int quantity);

    ShoppingCart removeItem(String buyerId, String productId);

    ShoppingCart clearCart(String buyerId);
}
