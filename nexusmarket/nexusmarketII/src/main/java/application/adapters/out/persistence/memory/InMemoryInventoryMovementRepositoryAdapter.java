package application.adapters.out.persistence.memory;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.ports.out.InventoryMovementRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

/**
 * In-memory adapter for the InventoryMovementRepository output port
 * (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryInventoryMovementRepositoryAdapter implements InventoryMovementRepository {

    private final InMemoryDataStore store;

    public InMemoryInventoryMovementRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(InventoryMovement movement) {
        store.movements.add(movement);
    }

    @Override
    public List<InventoryMovement> findByInventory(Inventory inventory) {
        synchronized (store.movements) {
            return store.movements.stream()
                    .filter(movement -> movement.getInventory().getIdentifier().equals(inventory.getIdentifier()))
                    .sorted(Comparator.comparing(InventoryMovement::getMovementId))
                    .toList();
        }
    }
}
