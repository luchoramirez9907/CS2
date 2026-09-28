package Application.adapters.out.persistence.memory;

import Application.domain.models.Order;
import Application.domain.ports.out.OrderRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * In-memory adapter for the OrderRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryOrderRepositoryAdapter implements OrderRepository {

    private final InMemoryDataStore store;

    public InMemoryOrderRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(Order order) {
        store.orders.put(order.getOrderId(), order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.orders.get(orderId));
    }

    @Override
    public List<Order> findByBuyerId(String buyerId) {
        return store.orders.values().stream()
                .filter(order -> order.getBuyer().getIdentifier().equals(buyerId))
                .toList();
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(store.orders.values());
    }
}
