package Application.adapters.out.persistence.memory;

import Application.domain.models.ShoppingCart;
import Application.domain.ports.out.ShoppingCartRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * In-memory adapter for the ShoppingCartRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryShoppingCartRepositoryAdapter implements ShoppingCartRepository {

    private final InMemoryDataStore store;

    public InMemoryShoppingCartRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(ShoppingCart cart) {
        store.carts.put(cart.getCartId(), cart);
    }

    @Override
    public Optional<ShoppingCart> findById(String cartId) {
        return Optional.ofNullable(store.carts.get(cartId));
    }

    @Override
    public Optional<ShoppingCart> findActiveByBuyerId(String buyerId) {
        return store.carts.values().stream()
                .filter(cart -> cart.getBuyer().getIdentifier().equals(buyerId))
                .findFirst();
    }

    @Override
    public void delete(ShoppingCart cart) {
        store.carts.remove(cart.getCartId());
    }
}
