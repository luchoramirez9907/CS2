package application.domain.services.warehouse;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Person;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.Address;

import java.util.Comparator;
import java.util.List;

/**
 * ManageWarehouseService
 *
 * Catalog service "Manage Warehouse". Consults,
 * updates and lists warehouses; a seller only operates on its own
 * warehouses.
 */
public class ManageWarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public ManageWarehouseService(WarehouseRepository warehouseRepository,
                                  ValidatePermissionsService permissionsService,
                                  ValidateOwnershipAccessService ownershipService) {
        if (warehouseRepository == null || permissionsService == null || ownershipService == null) {
            throw new IllegalArgumentException("ManageWarehouseService requires its dependencies");
        }
        this.warehouseRepository = warehouseRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    public Warehouse consultWarehouse(String requesterId, String warehouseId) {
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.CONSULT_WAREHOUSE);
        Warehouse warehouse = findWarehouse(warehouseId);
        ownershipService.requireWarehouseAccess(requester, warehouse);
        return warehouse;
    }

    /**
     * @param address new location (null keeps the current one)
     */
    public Warehouse updateWarehouse(String requesterId, String warehouseId, String name, Address address) {
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.UPDATE_WAREHOUSE);
        Warehouse warehouse = findWarehouse(warehouseId);
        ownershipService.requireWarehouseAccess(requester, warehouse);
        // A null address keeps the current location.
        warehouse.updateInformation(name, address == null ? warehouse.getAddress() : address);
        warehouseRepository.save(warehouse);
        return warehouse;
    }

    /**
     * Lists the warehouses the requester may access: every warehouse for
     * Administrators, Supervisors and Logistics Operators; only its own
     * warehouses for a Seller.
     */
    public List<Warehouse> listWarehouses(String requesterId) {
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.CONSULT_WAREHOUSE);
        return warehouseRepository.findAll().stream()
                .filter(warehouse -> ownershipService.canAccessWarehouse(requester, warehouse))
                .sorted(Comparator.comparing(Warehouse::getIdentifier))
                .toList();
    }

    private Warehouse findWarehouse(String warehouseId) {
        ServiceValidations.requireText(warehouseId, "warehouse id");
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
    }
}
