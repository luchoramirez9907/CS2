package Application.domain.services;

import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.models.Administrator;
import Application.domain.models.MarketplaceWarehouse;
import Application.domain.models.Person;
import Application.domain.models.Seller;
import Application.domain.models.SellerWarehouse;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ManageWarehouseUseCase;
import Application.domain.ports.in.RegisterWarehouseUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.SellerRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.SystemRole;

/**
 * WarehouseManagementService
 *
 * Implements the Warehouse Management services (per SDD - Services):
 * - Register Warehouse: creates a warehouse owned directly by the
 *   Marketplace (registered by an Administrator) or owned by a seller
 *   (registered by the seller itself).
 * - Manage Warehouse: consults and updates the information of an
 *   existing warehouse.
 */
public class WarehouseManagementService implements RegisterWarehouseUseCase, ManageWarehouseUseCase {

    private final PersonRepository personRepository;
    private final SellerRepository sellerRepository;
    private final WarehouseRepository warehouseRepository;
    private final NotificationService notificationService;

    public WarehouseManagementService(PersonRepository personRepository,
                                      SellerRepository sellerRepository,
                                      WarehouseRepository warehouseRepository,
                                      NotificationService notificationService) {
        if (personRepository == null || sellerRepository == null
                || warehouseRepository == null || notificationService == null) {
            throw new IllegalArgumentException("WarehouseManagementService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Warehouse registerMarketplaceWarehouse(String performerId, String warehouseId,
                                                  String name, Address address) {
        Person performer = requirePerformer(performerId, warehouseId, name, address);
        performer.requireRole("register a Marketplace warehouse", SystemRole.ADMINISTRATOR);
        requireUniqueIdentifier(warehouseId);

        MarketplaceWarehouse warehouse = new MarketplaceWarehouse(
                warehouseId, name, address, (Administrator) performer);
        warehouseRepository.save(warehouse);
        notificationService.notify(performer, "Warehouse registered",
                "Marketplace warehouse '" + warehouse.getName() + "' was registered");
        return warehouse;
    }

    @Override
    public Warehouse registerSellerWarehouse(String performerId, String warehouseId,
                                             String name, Address address) {
        Person performer = requirePerformer(performerId, warehouseId, name, address);
        performer.requireRole("register a seller warehouse", SystemRole.SELLER);
        Seller seller = sellerRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seller '" + performerId + "' does not exist"));
        requireUniqueIdentifier(warehouseId);

        SellerWarehouse warehouse = new SellerWarehouse(warehouseId, name, address, seller);
        warehouseRepository.save(warehouse);
        seller.addWarehouse(warehouse);
        sellerRepository.save(seller);
        notificationService.notify(seller, "Warehouse registered",
                "Your warehouse '" + warehouse.getName() + "' was registered");
        return warehouse;
    }

    @Override
    public Warehouse consultWarehouse(String requesterId, String warehouseId) {
        requireText(requesterId, "requester id");
        requireText(warehouseId, "warehouse id");

        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse '" + warehouseId + "' does not exist"));
    }

    @Override
    public Warehouse updateWarehouse(String performerId, String warehouseId, String newName) {
        requireText(performerId, "performer id");
        requireText(warehouseId, "warehouse id");
        requireText(newName, "new warehouse name");

        Person performer = personRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Performer '" + performerId + "' does not exist"));
        performer.requireActive();

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse '" + warehouseId + "' does not exist"));

        if (warehouse instanceof MarketplaceWarehouse) {
            performer.requireRole("update a Marketplace warehouse", SystemRole.ADMINISTRATOR);
        } else if (warehouse instanceof SellerWarehouse sellerWarehouse
                && !sellerWarehouse.getOwner().getIdentifier().equals(performerId)) {
            throw new InvalidRoleAssignmentException("update a seller warehouse",
                    performer.getRole(), "the owning SELLER or an ADMINISTRATOR");
        }

        warehouse.rename(newName);
        warehouseRepository.save(warehouse);
        notificationService.notify(performer, "Warehouse updated",
                "Warehouse '" + warehouse.getIdentifier() + "' is now named '"
                        + warehouse.getName() + "'");
        return warehouse;
    }

    private Person requirePerformer(String performerId, String warehouseId,
                                    String name, Address address) {
        requireText(performerId, "performer id");
        requireText(warehouseId, "warehouse identifier");
        requireText(name, "warehouse name");
        if (address == null) {
            throw new IllegalArgumentException("A warehouse requires a physical address");
        }
        Person performer = personRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Performer '" + performerId + "' does not exist"));
        performer.requireActive();
        return performer;
    }

    private void requireUniqueIdentifier(String warehouseId) {
        if (warehouseRepository.findById(warehouseId).isPresent()) {
            throw new IllegalArgumentException("A warehouse with identifier '"
                    + warehouseId + "' already exists");
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
