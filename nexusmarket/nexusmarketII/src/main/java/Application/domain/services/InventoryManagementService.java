package Application.domain.services;

import Application.domain.models.Inventory;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ConsultInventoryUseCase;
import Application.domain.ports.in.RegisterInventoryMovementUseCase;
import Application.domain.ports.out.InventoryMovementRepository;
import Application.domain.ports.out.InventoryRepository;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.valueobjects.SystemRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * InventoryManagementService
 *
 * Implements the Inventory Management services (per SDD - Services):
 * Register Inventory Movement (initial stock, adjustments) and Consult
 * Inventory. Only a LogisticsOperator registers movements; stock is
 * never negative and every change generates a movement.
 */
public class InventoryManagementService implements RegisterInventoryMovementUseCase, ConsultInventoryUseCase {

    private final PersonRepository personRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final NotificationService notificationService;

    public InventoryManagementService(PersonRepository personRepository,
                                      ProductRepository productRepository,
                                      WarehouseRepository warehouseRepository,
                                      InventoryRepository inventoryRepository,
                                      InventoryMovementRepository movementRepository,
                                      NotificationService notificationService) {
        if (personRepository == null || productRepository == null || warehouseRepository == null
                || inventoryRepository == null || movementRepository == null
                || notificationService == null) {
            throw new IllegalArgumentException("InventoryManagementService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Inventory registerStockIn(String performerId, String productId,
                                     String warehouseId, int quantity) {
        requireText(performerId, "performer id");
        requireText(productId, "product id");
        requireText(warehouseId, "warehouse id");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock-in quantity must be positive");
        }

        Person performer = requireLogisticsOperator(performerId);
        Product product = requirePhysicalProduct(productId);
        Warehouse warehouse = requireWarehouse(warehouseId);

        Inventory inventory = findRecord(productId, warehouseId)
                .orElseGet(() -> new Inventory(UUID.randomUUID().toString(),
                        product, warehouse, 0, 0));
        inventory.stockIn(quantity, performer);
        persist(inventory);
        notificationService.notify(performer, "Stock registered",
                quantity + " unit(s) of product '" + product.getName()
                        + "' entered warehouse '" + warehouse.getName() + "'");
        return inventory;
    }

    @Override
    public Inventory registerAdjustment(String performerId, String productId,
                                        String warehouseId, int signedDelta) {
        requireText(performerId, "performer id");
        requireText(productId, "product id");
        requireText(warehouseId, "warehouse id");
        if (signedDelta == 0) {
            throw new IllegalArgumentException("Adjustment delta must not be zero");
        }

        Person performer = requireLogisticsOperator(performerId);
        requirePhysicalProduct(productId);
        requireWarehouse(warehouseId);

        Inventory inventory = findRecord(productId, warehouseId)
                .orElseThrow(() -> new IllegalStateException(
                        "No inventory record exists for product '" + productId
                                + "' at warehouse '" + warehouseId + "'"));
        inventory.adjust(signedDelta, performer);
        persist(inventory);
        notificationService.notify(performer, "Stock adjusted",
                "Inventory '" + inventory.getIdentifier() + "' adjusted by " + signedDelta);
        return inventory;
    }

    @Override
    public StockSnapshot consultStock(String requesterId, String productId, String warehouseId) {
        requireText(requesterId, "requester id");
        requireText(productId, "product id");
        requireText(warehouseId, "warehouse id");

        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();

        Inventory inventory = findRecord(productId, warehouseId)
                .orElseThrow(() -> new IllegalStateException(
                        "No inventory record exists for product '" + productId
                                + "' at warehouse '" + warehouseId + "'"));
        return new StockSnapshot(inventory.getIdentifier(), productId, warehouseId,
                inventory.getAvailableQuantity(), inventory.getReservedQuantity());
    }

    private Optional<Inventory> findRecord(String productId, String warehouseId) {
        List<Inventory> records = inventoryRepository.findByProductId(productId);
        return records.stream()
                .filter(inventory -> inventory.getWarehouse().getIdentifier().equals(warehouseId))
                .findFirst();
    }

    private Person requireLogisticsOperator(String performerId) {
        Person performer = personRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Performer '" + performerId + "' does not exist"));
        performer.requireActive();
        performer.requireRole("register inventory movements", SystemRole.LOGISTICS_OPERATOR);
        return performer;
    }

    private Product requirePhysicalProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product '" + productId + "' does not exist"));
        if (!product.requiresPhysicalDispatch()) {
            throw new IllegalStateException("Digital products do not require inventory management");
        }
        return product;
    }

    private Warehouse requireWarehouse(String warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse '" + warehouseId + "' does not exist"));
    }

    private void persist(Inventory inventory) {
        inventoryRepository.save(inventory);
        inventory.drainPendingMovements().forEach(movementRepository::save);
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
