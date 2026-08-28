package Application.adapters.out.persistence.memory;

import Application.domain.models.Warehouse;
import Application.domain.ports.out.WarehouseRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * In-memory adapter for the WarehouseRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryWarehouseRepositoryAdapter implements WarehouseRepository {

    private final InMemoryDataStore store;

    public InMemoryWarehouseRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(Warehouse warehouse) {
        store.warehouses.put(warehouse.getIdentifier(), warehouse);
    }

    @Override
    public Optional<Warehouse> findById(String identifier) {
        return Optional.ofNullable(store.warehouses.get(identifier));
    }
}
