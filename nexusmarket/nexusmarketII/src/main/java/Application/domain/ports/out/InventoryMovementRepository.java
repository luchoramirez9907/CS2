package Application.domain.ports.out;

import Application.domain.models.InventoryMovement;

/**
 * Output port: persistence contract for the inventory movement history
 * (high-volume operational data, stored e.g. in MongoDB).
 */
public interface InventoryMovementRepository {

    void save(InventoryMovement movement);
}
