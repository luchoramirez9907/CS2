package application.domain.services.warehouse;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Administrator;
import application.domain.models.MarketplaceWarehouse;
import application.domain.models.Person;
import application.domain.models.Seller;
import application.domain.models.SellerWarehouse;
import application.domain.models.Warehouse;
import application.domain.ports.out.SellerRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.SystemRole;

/**
 * RegisterWarehouseService
 *
 * Catalog service "Register Warehouse". A
 * Marketplace warehouse is registered by an Administrator, who manages it;
 * a Seller warehouse is always linked to exactly one Seller, and a seller
 * may only register its own warehouses.
 */
public class RegisterWarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final SellerRepository sellerRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public RegisterWarehouseService(WarehouseRepository warehouseRepository,
                                    SellerRepository sellerRepository,
                                    ValidatePermissionsService permissionsService,
                                    ValidateOwnershipAccessService ownershipService) {
        if (warehouseRepository == null || sellerRepository == null || permissionsService == null
                || ownershipService == null) {
            throw new IllegalArgumentException("RegisterWarehouseService requires its dependencies");
        }
        this.warehouseRepository = warehouseRepository;
        this.sellerRepository = sellerRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    /**
     * @param requesterId   Administrator (any ownership) or Seller (own warehouse only)
     * @param ownerSellerId owner seller; null or blank for a Marketplace warehouse
     *                      (managed by the requesting Administrator)
     * @return the registered Warehouse
     */
    public Warehouse registerWarehouse(String requesterId, String identifier, String name,
                                       Address address, String ownerSellerId) {
        ServiceValidations.requireText(identifier, "warehouse identifier");
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.REGISTER_WAREHOUSE);
        if (warehouseRepository.findById(identifier).isPresent()) {
            throw new DuplicateResourceException("warehouse", identifier);
        }

        Warehouse warehouse;
        if (ownerSellerId == null || ownerSellerId.isBlank()) {
            requester.requireRole("register a Marketplace warehouse", SystemRole.ADMINISTRATOR);
            warehouse = new MarketplaceWarehouse(identifier, name, address, (Administrator) requester);
        } else {
            Seller owner = sellerRepository.findById(ownerSellerId)
                    .orElseThrow(() -> new EntityNotFoundException("Seller", ownerSellerId));
            owner.requireActive();
            ownershipService.requireSellerAccess(requester, owner);
            SellerWarehouse sellerWarehouse = new SellerWarehouse(identifier, name, address, owner);
            owner.addWarehouse(sellerWarehouse);
            warehouse = sellerWarehouse;
        }
        warehouseRepository.save(warehouse);
        return warehouse;
    }
}
