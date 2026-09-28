package Application.domain.ports.in;

import Application.domain.models.ShoppingCart;

/**
 * Input port (use case): Manage Cart Items.
 *
 * Adds, updates the quantity of, removes, or clears products within a
 * buyer's shopping cart, compensating stock reservations accordingly.
 */
public interface ManageCartItemsUseCase {

    /**
     * Sets the selection of a product to an absolute quantity. A
     * quantity increase reserves additional stock; a decrease releases
     * the surplus.
     */
    ShoppingCart updateItemQuantity(String buyerId, String cartId, String productId, int newQuantity);

    /** Removes the selection of a product, releasing its reservation. */
    ShoppingCart removeItem(String buyerId, String cartId, String productId);

    /** Clears every selection of the cart, releasing all reservations. */
    ShoppingCart clearCart(String buyerId, String cartId);
}
