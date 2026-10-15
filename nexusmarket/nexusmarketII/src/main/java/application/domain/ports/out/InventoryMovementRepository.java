package application.domain.ports.out;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;

import java.util.List;

/**
 * Output port: persistence contract for the inventory movement history
 * (high-volume operational data, stored e.g. in MongoDB).
 */
public interface InventoryMovementRepository {

    void save(InventoryMovement movement);

    /**
     * @return the movements of an inventory record, in chronological order
     */
    List<InventoryMovement> findByInventory(Inventory inventory);
}
