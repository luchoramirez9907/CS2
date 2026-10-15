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
import application.domain.valueobjects.SystemRole;

import java.util.List;

/**
 * ConsultInventoryService
 *
 * Catalog service "Consult Inventory". Retrieves
 * the available, reserved and damaged stock of a product at a warehouse,
 * and the movement history of that inventory record.
 */
public class ConsultInventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public ConsultInventoryService(InventoryRepository inventoryRepository,
                                   InventoryMovementRepository movementRepository,
                                   ProductRepository productRepository,
                                   WarehouseRepository warehouseRepository,
                                   ValidatePermissionsService permissionsService,
                                   ValidateOwnershipAccessService ownershipService) {
        if (inventoryRepository == null || movementRepository == null || productRepository == null
                || warehouseRepository == null || permissionsService == null || ownershipService == null) {
            throw new IllegalArgumentException("ConsultInventoryService requires its dependencies");
        }
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    public Inventory consultInventory(String requesterId, String productId, String warehouseId) {
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.CONSULT_INVENTORY);
        ServiceValidations.requireText(productId, "product id");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        ServiceValidations.requireText(warehouseId, "warehouse id");
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        requireStockAccess(requester, product, warehouse);
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("No inventory record exists for product '"
                        + productId + "' at warehouse '" + warehouseId + "'"));
    }

    /**
     * @return the movement history of the inventory record of a product at a
     *         warehouse, in chronological order
     */
    public List<InventoryMovement> consultMovementHistory(String requesterId, String productId,
                                                          String warehouseId) {
        Inventory inventory = consultInventory(requesterId, productId, warehouseId);
        return movementRepository.findByInventory(inventory);
    }

    /**
     * Sellers only consult stock of their own products; the other
     * authorized roles must be able to access the warehouse.
     */
    private void requireStockAccess(Person user, Product product, Warehouse warehouse) {
        if (user.getRole() == SystemRole.SELLER) {
            ownershipService.requireProductAccess(user, product);
            return;
        }
        ownershipService.requireWarehouseAccess(user, warehouse);
    }
}
