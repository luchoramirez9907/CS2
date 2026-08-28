package Application.domain.services;

import Application.domain.exceptions.InsufficientInventoryException;
import Application.domain.models.Inventory;
import Application.domain.models.InventoryMovement;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.ports.out.InventoryMovementRepository;
import Application.domain.ports.out.InventoryRepository;

import java.util.List;
import java.util.Optional;

/**
 * InventoryReservationService
 *
 * Domain service coordinating stock reservations, releases, sale exits
 * and returns across the distributed inventory of a product, guaranteeing
 * that every change generates a movement in the inventory movement history.
 */
public class InventoryReservationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryReservationService(InventoryRepository inventoryRepository,
                                       InventoryMovementRepository movementRepository) {
        if (inventoryRepository == null || movementRepository == null) {
            throw new IllegalArgumentException("InventoryReservationService requires its repositories");
        }
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
    }

    /**
     * Reserves stock of a product from the first inventory record with
     * enough availability.
     *
     * @throws InsufficientInventoryException when no record can satisfy the request
     */
    public Inventory reserveStock(Product product, int quantity, Person performedBy) {
        Inventory inventory = findRecordWithAvailability(product, quantity);
        inventory.reserve(quantity, performedBy);
        persist(inventory);
        return inventory;
    }

    /**
     * Releases a previous reservation (e.g. items removed from a cart).
     */
    public void releaseReservation(Product product, int quantity, Person performedBy) {
        Inventory inventory = findRecordWithReservation(product, quantity);
        inventory.releaseReservation(quantity, performedBy);
        persist(inventory);
    }

    /**
     * Confirms the sale of reserved stock when the order is fulfilled.
     */
    public void confirmSale(Product product, int quantity, Person performedBy) {
        Inventory inventory = findRecordWithReservation(product, quantity);
        inventory.confirmSale(quantity, performedBy);
        persist(inventory);
    }

    /**
     * Reinstates stock as a result of a product return.
     */
    public void reinstateStock(Product product, int quantity, Person performedBy) {
        List<Inventory> records = inventoryRepository.findByProductId(product.getIdentifier());
        if (records.isEmpty()) {
            throw new IllegalStateException("No inventory record exists for product '"
                    + product.getIdentifier() + "'");
        }
        Inventory inventory = records.get(0);
        inventory.reinstate(quantity, performedBy);
        persist(inventory);
    }

    private Inventory findRecordWithAvailability(Product product, int quantity) {
        List<Inventory> records = inventoryRepository.findByProductId(product.getIdentifier());
        for (Inventory inventory : records) {
            if (inventory.getAvailableQuantity() >= quantity) {
                return inventory;
            }
        }
        int best = records.stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .max()
                .orElse(0);
        throw new InsufficientInventoryException(product.getIdentifier(), quantity, best);
    }

    private Inventory findRecordWithReservation(Product product, int quantity) {
        List<Inventory> records = inventoryRepository.findByProductId(product.getIdentifier());
        for (Inventory inventory : records) {
            if (inventory.getReservedQuantity() >= quantity) {
                return inventory;
            }
        }
        throw new IllegalStateException("No inventory record of product '"
                + product.getIdentifier() + "' holds " + quantity + " reserved units");
    }

    private void persist(Inventory inventory) {
        inventoryRepository.save(inventory);
        for (InventoryMovement movement : inventory.drainPendingMovements()) {
            movementRepository.save(movement);
        }
    }

    /**
     * Total availability of a product across every warehouse.
     */
    public int totalAvailable(Product product) {
        return inventoryRepository.findByProductId(product.getIdentifier()).stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();
    }

    /**
     * Convenience lookup of a single inventory record.
     */
    public Optional<Inventory> findInventory(String inventoryId) {
        return inventoryRepository.findById(inventoryId);
    }
}
