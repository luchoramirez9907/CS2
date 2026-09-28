package Application.domain.models;

import Application.domain.exceptions.InsufficientInventoryException;
import Application.domain.valueobjects.InventoryMovementType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Inventory
 *
 * Represents the distributed stock of a product available for
 * commercialization at a specific warehouse.
 *
 * Business rules: belongs to exactly one Product and one Warehouse;
 * negative stock is never permitted; stock marked as damaged is kept
 * apart from the available stock, so it can never be reserved; every
 * change generates an InventoryMovement.
 */
public class Inventory {

    private final String identifier;
    private final Product product;
    private final Warehouse warehouse;
    private int availableQuantity;
    private int reservedQuantity;
    private int damagedQuantity;
    private int movementSequence;
    private final List<InventoryMovement> movements = new ArrayList<>();
    private final List<InventoryMovement> pendingMovements = new ArrayList<>();

    public Inventory(String identifier, Product product, Warehouse warehouse,
                     int availableQuantity, int reservedQuantity) {
        this(identifier, product, warehouse, availableQuantity, reservedQuantity, 0);
    }

    public Inventory(String identifier, Product product, Warehouse warehouse,
                     int availableQuantity, int reservedQuantity, int damagedQuantity) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Inventory identifier must not be null or blank");
        }
        if (product == null) {
            throw new IllegalArgumentException("Inventory must be linked to exactly one Product");
        }
        if (warehouse == null) {
            throw new IllegalArgumentException("Inventory must be linked to exactly one Warehouse");
        }
        if (availableQuantity < 0 || reservedQuantity < 0 || damagedQuantity < 0) {
            throw new IllegalArgumentException("Negative stock is never permitted");
        }
        this.identifier = identifier;
        this.product = product;
        this.warehouse = warehouse;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.damagedQuantity = damagedQuantity;
    }

    public String getIdentifier() { return identifier; }

    public Product getProduct() { return product; }

    public Warehouse getWarehouse() { return warehouse; }

    public int getAvailableQuantity() { return availableQuantity; }

    public int getReservedQuantity() { return reservedQuantity; }

    public int getDamagedQuantity() { return damagedQuantity; }

    public int getTotalQuantity() { return availableQuantity + reservedQuantity; }

    public List<InventoryMovement> getMovements() {
        return Collections.unmodifiableList(movements);
    }

    /**
     * Returns and clears the movements generated since the last drain, so
     * adapters can persist them (e.g. into the movement history store).
     */
    public List<InventoryMovement> drainPendingMovements() {
        List<InventoryMovement> drained = new ArrayList<>(pendingMovements);
        pendingMovements.clear();
        return drained;
    }

    /** Registers incoming stock into the warehouse. */
    public void stockIn(int quantity, Person performedBy) {
        requirePositive(quantity, "stock in");
        availableQuantity += quantity;
        registerMovement(InventoryMovementType.STOCK_IN, quantity, performedBy);
    }

    /**
     * Reserves stock as a result of an active shopping cart or order.
     *
     * @throws InsufficientInventoryException when there is not enough stock
     */
    public void reserve(int quantity, Person performedBy) {
        requirePositive(quantity, "reservation");
        if (availableQuantity < quantity) {
            throw new InsufficientInventoryException(product.getIdentifier(), quantity, availableQuantity);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
        registerMovement(InventoryMovementType.RESERVATION, quantity, performedBy);
    }

    /** Releases a previous reservation (e.g. items removed from a cart). */
    public void releaseReservation(int quantity, Person performedBy) {
        requirePositive(quantity, "reservation release");
        if (reservedQuantity < quantity) {
            throw new IllegalArgumentException("Cannot release " + quantity
                    + " units: only " + reservedQuantity + " are reserved");
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
        registerMovement(InventoryMovementType.RESERVATION, -quantity, performedBy);
    }

    /** Confirms the sale of previously reserved stock (completed sale). */
    public void confirmSale(int quantity, Person performedBy) {
        requirePositive(quantity, "sale exit");
        if (reservedQuantity < quantity) {
            throw new IllegalArgumentException("Cannot confirm sale of " + quantity
                    + " units: only " + reservedQuantity + " are reserved");
        }
        reservedQuantity -= quantity;
        registerMovement(InventoryMovementType.SALE_EXIT, quantity, performedBy);
    }

    /**
     * Applies a manual correction of the recorded stock quantity. The
     * adjustment is a signed delta and must never drive stock negative.
     */
    public void adjust(int signedDelta, Person performedBy) {
        if (signedDelta == 0) {
            throw new IllegalArgumentException("Adjustment delta must not be zero");
        }
        if (availableQuantity + signedDelta < 0) {
            throw new IllegalArgumentException("Adjustment would drive available stock negative "
                    + "(current: " + availableQuantity + ", delta: " + signedDelta + ")");
        }
        availableQuantity += signedDelta;
        registerMovement(InventoryMovementType.ADJUSTMENT, signedDelta, performedBy);
    }

    /**
     * Marks available stock as damaged. Damaged units leave the available
     * stock (recorded as a negative ADJUSTMENT) and can never be reserved.
     */
    public void markDamaged(int quantity, Person performedBy) {
        requirePositive(quantity, "damage");
        if (availableQuantity < quantity) {
            throw new InsufficientInventoryException(product.getIdentifier(), quantity, availableQuantity);
        }
        availableQuantity -= quantity;
        damagedQuantity += quantity;
        registerMovement(InventoryMovementType.ADJUSTMENT, -quantity, performedBy);
    }

    /** Reinstates stock as a result of a product return. */
    public void reinstate(int quantity, Person performedBy) {
        requirePositive(quantity, "return reinstatement");
        availableQuantity += quantity;
        registerMovement(InventoryMovementType.RETURN, quantity, performedBy);
    }

    private void requirePositive(int quantity, String operation) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Inventory " + operation + " quantity must be positive");
        }
    }

    /**
     * Every change to an inventory record generates an InventoryMovement.
     */
    private void registerMovement(InventoryMovementType type, int quantity, Person performedBy) {
        InventoryMovement movement = new InventoryMovement(
                ++movementSequence, type, quantity, LocalDateTime.now(), this, performedBy);
        movements.add(movement);
        pendingMovements.add(movement);
    }
}

