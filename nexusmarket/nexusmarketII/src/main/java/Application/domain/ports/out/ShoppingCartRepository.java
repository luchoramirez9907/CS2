package Application.domain.ports.out;

import Application.domain.models.ShoppingCart;

import java.util.Optional;

/**
 * Output port: persistence contract for active ShoppingCarts.
 */
public interface ShoppingCartRepository {

    void save(ShoppingCart cart);

    Optional<ShoppingCart> findById(String cartId);

    Optional<ShoppingCart> findActiveByBuyerId(String buyerId);

    void delete(ShoppingCart cart);
}
