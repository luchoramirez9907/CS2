package Application.domain.ports.out;

import Application.domain.models.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence contract for Inventory records.
 */
public interface InventoryRepository {

    void save(Inventory inventory);

    Optional<Inventory> findById(String identifier);

    List<Inventory> findByProductId(String productId);

    Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId);

    List<Inventory> findAll();
}
