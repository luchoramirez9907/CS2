package application.domain.services.inventory;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryMovementRepository;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.SystemRole;

import java.util.UUID;

/**
 * RegisterInventoryMovementService
 *
 * Catalog service "Register Inventory Movement". Every significant
 * change to an inventory record (initial stock, reservation, reservation release, manual
 * adjustment, return or damage) is applied through the Inventory aggregate,
 * which guarantees that stock is never reserved or reduced below zero and
 * that damaged stock is never reserved; each change is persisted in the
 * movement history.
 *
 * Only physical products hold inventory. SALE_EXIT movements are recorded
 * automatically when a shipment is dispatched.
 */
public class RegisterInventoryMovementService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public RegisterInventoryMovementService(InventoryRepository inventoryRepository,
                                            InventoryMovementRepository movementRepository,
                                            ProductRepository productRepository,
                                            WarehouseRepository warehouseRepository,
                                            ValidatePermissionsService permissionsService,
                                            ValidateOwnershipAccessService ownershipService) {
        if (inventoryRepository == null || movementRepository == null || productRepository == null
                || warehouseRepository == null || permissionsService == null || ownershipService == null) {
            throw new IllegalArgumentException("RegisterInventoryMovementService requires its dependencies");
        }
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    /**
     * @param type     STOCK_IN (initial/incoming stock; creates the record when missing),
     *                 RESERVATION (positive quantity reserves, negative releases),
     *                 ADJUSTMENT (signed manual correction) or RETURN (reinstatement)
     * @param quantity quantity affected by the movement
     * @return the updated Inventory record
     */
    public Inventory registerMovement(String requesterId, String productId, String warehouseId,
                                      InventoryMovementType type, int quantity) {
        if (type == null) {
            throw new IllegalArgumentException("Movement type must not be null");
        }
        if (quantity == 0) {
            throw new IllegalArgumentException("Movement quantity must not be zero");
        }
        Person performer = permissionsService.requirePermission(requesterId,
                BusinessOperation.REGISTER_INVENTORY_MOVEMENT);
        Product product = findPhysicalProduct(productId);
        Warehouse warehouse = findWarehouse(warehouseId);
        requireStockAccess(performer, product, warehouse);

        Inventory inventory;
        if (InventoryMovementType.STOCK_IN.equals(type)) {
            inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                    .orElseGet(() -> newInventoryRecord(product, warehouse));
            inventory.stockIn(quantity, performer);
        } else {
            inventory = findInventory(productId, warehouseId);
            if (InventoryMovementType.RESERVATION.equals(type)) {
                inventory.reserve(quantity, performer);
            } else if (InventoryMovementType.RESERVATION_RELEASE.equals(type)) {
                inventory.releaseReservation(quantity, performer);
            } else if (InventoryMovementType.DAMAGE.equals(type)) {
                inventory.markDamaged(quantity, performer);
            } else if (InventoryMovementType.ADJUSTMENT.equals(type)) {
                inventory.adjust(quantity, performer);
            } else if (InventoryMovementType.RETURN.equals(type)) {
                inventory.reinstate(quantity, performer);
            } else {
                throw new IllegalArgumentException("Movement type " + type.getCode()
                        + " cannot be registered manually (it is recorded when a shipment is dispatched)");
            }
        }
        persist(inventory);
        return inventory;
    }

    /**
     * Marks available stock as damaged; damaged units cannot be reserved.
     */
    public Inventory registerDamagedStock(String requesterId, String productId, String warehouseId,
                                          int quantity) {
        Person performer = permissionsService.requirePermission(requesterId,
                BusinessOperation.REGISTER_INVENTORY_MOVEMENT);
        Product product = findPhysicalProduct(productId);
        Warehouse warehouse = findWarehouse(warehouseId);
        requireStockAccess(performer, product, warehouse);

        Inventory inventory = findInventory(productId, warehouseId);
        inventory.markDamaged(quantity, performer);
        persist(inventory);
        return inventory;
    }

    /**
     * Sellers only operate on stock of their own products; the other
     * authorized roles must be able to access the warehouse.
     */
    private void requireStockAccess(Person user, Product product, Warehouse warehouse) {
        if (user.getRole() == SystemRole.SELLER) {
            ownershipService.requireProductAccess(user, product);
            return;
        }
        ownershipService.requireWarehouseAccess(user, warehouse);
    }

    private Inventory newInventoryRecord(Product product, Warehouse warehouse) {
        Inventory inventory = new Inventory(UUID.randomUUID().toString(), product, warehouse, 0, 0);
        warehouse.addInventoryRecord(inventory);
        return inventory;
    }

    private Inventory findInventory(String productId, String warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("No inventory record exists for product '"
                        + productId + "' at warehouse '" + warehouseId + "'"));
    }

    private Product findPhysicalProduct(String productId) {
        ServiceValidations.requireText(productId, "product id");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (!product.requiresPhysicalDispatch()) {
            throw new IllegalArgumentException("Product '" + productId
                    + "' is digital and does not require inventory");
        }
        return product;
    }

    private Warehouse findWarehouse(String warehouseId) {
        ServiceValidations.requireText(warehouseId, "warehouse id");
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
    }

    private void persist(Inventory inventory) {
        inventoryRepository.save(inventory);
        for (InventoryMovement movement : inventory.drainPendingMovements()) {
            movementRepository.save(movement);
        }
    }
}
