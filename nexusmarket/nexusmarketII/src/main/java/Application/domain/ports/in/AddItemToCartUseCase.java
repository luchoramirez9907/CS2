package Application.domain.ports.in;

import Application.domain.models.ShoppingCart;

import java.math.BigDecimal;

/**
 * Input port (use case): adds a product selection to the buyer's active
 * shopping cart, creating the cart when it does not exist yet.
 */
public interface AddItemToCartUseCase {

    /**
     * @param buyerId   identifier of the Buyer
     * @param cartId    identifier of the active cart (optional; resolved when blank)
     * @param productId identifier of the Product to add
     * @param quantity  quantity to add
     * @param unitPrice reference unit price at selection time
     * @return the updated ShoppingCart
     */
    ShoppingCart addItemToCart(String buyerId, String cartId, String productId,
                               int quantity, BigDecimal unitPrice);
}
