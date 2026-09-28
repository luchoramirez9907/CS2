package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Inventory;
import Application.domain.models.InventoryMovement;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ConsultInventoryUseCase;
import Application.domain.ports.in.RegisterInventoryMovementUseCase;
import Application.domain.ports.out.InventoryMovementRepository;
import Application.domain.ports.out.InventoryRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.valueobjects.InventoryMovementType;
import Application.domain.valueobjects.SystemRole;

import java.util.UUID;

/**
 * InventoryManagementService
 *
 * Implements RegisterInventoryMovementUseCase and ConsultInventoryUseCase.
 * Every significant change to an inventory record (initial stock,
 * reservation, reservation release, manual adjustment, return or damage)
 * is applied through the Inventory aggregate, which guarantees that stock
 * is never reserved or reduced below zero and that damaged stock is never
 * reserved; each change is persisted in the movement history.
 *
 * Only physical products hold inventory. SALE_EXIT movements are recorded
 * automatically when a shipment is dispatched.
 */
public class InventoryManagementService implements RegisterInventoryMovementUseCase, ConsultInventoryUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuthorizationService authorizationService;

    public InventoryManagementService(InventoryRepository inventoryRepository,
                                      InventoryMovementRepository movementRepository,
                                      ProductRepository productRepository,
                                      WarehouseRepository warehouseRepository,
                                      AuthorizationService authorizationService) {
        if (inventoryRepository == null || movementRepository == null || productRepository == null
                || warehouseRepository == null || authorizationService == null) {
            throw new IllegalArgumentException("InventoryManagementService requires its dependencies");
        }
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public Inventory registerMovement(String requesterId, String productId, String warehouseId,
                                      InventoryMovementType type, int quantity) {
        if (type == null) {
            throw new IllegalArgumentException("Movement type must not be null");
        }
        if (quantity == 0) {
            throw new IllegalArgumentException("Movement quantity must not be zero");
        }
        Person performer = authorizationService.requirePermission(requesterId,
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
                if (quantity > 0) {
                    inventory.reserve(quantity, performer);
                } else {
                    inventory.releaseReservation(-quantity, performer);
                }
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

    @Override
    public Inventory registerDamagedStock(String requesterId, String productId, String warehouseId,
                                          int quantity) {
        Person performer = authorizationService.requirePermission(requesterId,
                BusinessOperation.REGISTER_INVENTORY_MOVEMENT);
        Product product = findPhysicalProduct(productId);
        Warehouse warehouse = findWarehouse(warehouseId);
        requireStockAccess(performer, product, warehouse);

        Inventory inventory = findInventory(productId, warehouseId);
        inventory.markDamaged(quantity, performer);
        persist(inventory);
        return inventory;
    }

    @Override
    public Inventory consultInventory(String requesterId, String productId, String warehouseId) {
        Person requester = authorizationService.requirePermission(requesterId,
                BusinessOperation.CONSULT_INVENTORY);
        ServiceValidations.requireText(productId, "product id");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product '" + productId + "' does not exist"));
        Warehouse warehouse = findWarehouse(warehouseId);
        requireStockAccess(requester, product, warehouse);
        return findInventory(productId, warehouseId);
    }

    /**
     * Sellers only operate on stock of their own products; the other
     * authorized roles must be able to access the warehouse.
     */
    private void requireStockAccess(Person user, Product product, Warehouse warehouse) {
        if (user.getRole() == SystemRole.SELLER) {
            authorizationService.requireProductAccess(user, product);
            return;
        }
        authorizationService.requireWarehouseAccess(user, warehouse);
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
                .orElseThrow(() -> new IllegalArgumentException("Product '" + productId + "' does not exist"));
        if (!product.requiresPhysicalDispatch()) {
            throw new IllegalArgumentException("Product '" + productId
                    + "' is digital and does not require inventory");
        }
        return product;
    }

    private Warehouse findWarehouse(String warehouseId) {
        ServiceValidations.requireText(warehouseId, "warehouse id");
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse '" + warehouseId + "' does not exist"));
    }

    private void persist(Inventory inventory) {
        inventoryRepository.save(inventory);
        for (InventoryMovement movement : inventory.drainPendingMovements()) {
            movementRepository.save(movement);
        }
    }
}
