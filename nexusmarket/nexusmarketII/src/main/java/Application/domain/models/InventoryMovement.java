package Application.domain.models;

import Application.domain.valueobjects.InventoryMovementType;

import java.time.LocalDateTime;

/**
 * InventoryMovement
 *
 * Represents a significant change in an inventory record, providing
 * traceability of stock over time.
 */
public class InventoryMovement {

    private final Integer movementId;
    private final InventoryMovementType movementType;
    private final int quantity;
    private final LocalDateTime movementDate;
    private final Inventory inventory;
    private final Person performedBy;

    public InventoryMovement(Integer movementId, InventoryMovementType movementType, int quantity,
                             LocalDateTime movementDate, Inventory inventory, Person performedBy) {
        if (movementId == null || movementId <= 0) {
            throw new IllegalArgumentException("Movement id must be a positive number");
        }
        if (movementType == null) {
            throw new IllegalArgumentException("Movement type must not be null");
        }
        if (quantity == 0) {
            throw new IllegalArgumentException("Movement quantity must not be zero");
        }
        if (movementDate == null) {
            throw new IllegalArgumentException("Movement date must not be null");
        }
        if (inventory == null) {
            throw new IllegalArgumentException("Movement must affect exactly one Inventory record");
        }
        if (performedBy == null) {
            throw new IllegalArgumentException("Every operation must be executed by an authenticated Person");
        }
        this.movementId = movementId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.movementDate = movementDate;
        this.inventory = inventory;
        this.performedBy = performedBy;
    }

    public Integer getMovementId() {
        return movementId;
    }

    public InventoryMovementType getMovementType() {
        return movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Person getPerformedBy() {
        return performedBy;
    }
}
