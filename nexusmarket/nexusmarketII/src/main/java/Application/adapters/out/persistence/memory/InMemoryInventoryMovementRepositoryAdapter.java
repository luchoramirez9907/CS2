package Application.adapters.out.persistence.memory;

import Application.domain.models.InventoryMovement;
import Application.domain.ports.out.InventoryMovementRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

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
}
