package Application.adapters.out.persistence.memory;

import Application.domain.models.Inventory;
import Application.domain.ports.out.InventoryRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * In-memory adapter for the InventoryRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryInventoryRepositoryAdapter implements InventoryRepository {

    private final InMemoryDataStore store;

    public InMemoryInventoryRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(Inventory inventory) {
        store.inventories.put(inventory.getIdentifier(), inventory);
    }

    @Override
    public Optional<Inventory> findById(String identifier) {
        return Optional.ofNullable(store.inventories.get(identifier));
    }

    @Override
    public List<Inventory> findByProductId(String productId) {
        return store.inventories.values().stream()
                .filter(inventory -> inventory.getProduct().getIdentifier().equals(productId))
                .toList();
    }
}
