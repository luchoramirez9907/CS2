package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Administrator;
import Application.domain.models.MarketplaceWarehouse;
import Application.domain.models.Person;
import Application.domain.models.Seller;
import Application.domain.models.SellerWarehouse;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ManageWarehouseUseCase;
import Application.domain.ports.in.RegisterWarehouseUseCase;
import Application.domain.ports.out.SellerRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.SystemRole;

/**
 * WarehouseManagementService
 *
 * Implements RegisterWarehouseUseCase and ManageWarehouseUseCase. A
 * Marketplace warehouse is registered and managed by an Administrator; a
 * Seller warehouse is always linked to exactly one Seller, and a seller
 * may only register or update its own warehouses.
 */
public class WarehouseManagementService implements RegisterWarehouseUseCase, ManageWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;
    private final SellerRepository sellerRepository;
    private final AuthorizationService authorizationService;

    public WarehouseManagementService(WarehouseRepository warehouseRepository,
                                      SellerRepository sellerRepository,
                                      AuthorizationService authorizationService) {
        if (warehouseRepository == null || sellerRepository == null || authorizationService == null) {
            throw new IllegalArgumentException("WarehouseManagementService requires its dependencies");
        }
        this.warehouseRepository = warehouseRepository;
        this.sellerRepository = sellerRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public Warehouse registerWarehouse(String requesterId, String identifier, String name,
                                       Address address, String ownerSellerId) {
        ServiceValidations.requireText(identifier, "warehouse identifier");
        Person requester = authorizationService.requirePermission(requesterId,
                BusinessOperation.REGISTER_WAREHOUSE);
        if (warehouseRepository.findById(identifier).isPresent()) {
            throw new IllegalArgumentException("A warehouse with identifier '" + identifier + "' already exists");
        }

        Warehouse warehouse;
        if (ownerSellerId == null || ownerSellerId.isBlank()) {
            requester.requireRole("register a Marketplace warehouse", SystemRole.ADMINISTRATOR);
            warehouse = new MarketplaceWarehouse(identifier, name, address, (Administrator) requester);
        } else {
            Seller owner = sellerRepository.findById(ownerSellerId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Seller '" + ownerSellerId + "' does not exist"));
            owner.requireActive();
            authorizationService.requireSellerAccess(requester, owner);
            SellerWarehouse sellerWarehouse = new SellerWarehouse(identifier, name, address, owner);
            owner.addWarehouse(sellerWarehouse);
            warehouse = sellerWarehouse;
        }
        warehouseRepository.save(warehouse);
        return warehouse;
    }

    @Override
    public Warehouse consultWarehouse(String requesterId, String warehouseId) {
        Person requester = authorizationService.requirePermission(requesterId,
                BusinessOperation.CONSULT_WAREHOUSE);
        Warehouse warehouse = findWarehouse(warehouseId);
        authorizationService.requireWarehouseAccess(requester, warehouse);
        return warehouse;
    }

    @Override
    public Warehouse updateWarehouse(String requesterId, String warehouseId, String name, Address address) {
        Person requester = authorizationService.requirePermission(requesterId,
                BusinessOperation.UPDATE_WAREHOUSE);
        Warehouse warehouse = findWarehouse(warehouseId);
        authorizationService.requireWarehouseAccess(requester, warehouse);
        warehouse.updateInformation(name, address);
        warehouseRepository.save(warehouse);
        return warehouse;
    }

    private Warehouse findWarehouse(String warehouseId) {
        ServiceValidations.requireText(warehouseId, "warehouse id");
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse '" + warehouseId + "' does not exist"));
    }
}
