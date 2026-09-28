package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.models.MarketplaceWarehouse;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.SellerWarehouse;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ValidateOwnershipAccessUseCase;
import Application.domain.ports.in.ValidatePermissionsUseCase;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.valueobjects.SystemRole;

import java.util.stream.Collectors;

/**
 * AuthorizationService
 *
 * Implements the Authorization services (per SDD - Services):
 * - Validate Permissions: determines whether a user may perform a
 *   specific business operation based on role and status (RG-02/RG-03).
 * - Validate Ownership Access: determines whether a user may access
 *   information belonging to a buyer, seller, warehouse or order that
 *   it owns or manages.
 */
public class AuthorizationService implements ValidatePermissionsUseCase, ValidateOwnershipAccessUseCase {

    private final PersonRepository personRepository;
    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;

    public AuthorizationService(PersonRepository personRepository,
                                OrderRepository orderRepository,
                                WarehouseRepository warehouseRepository) {
        if (personRepository == null || orderRepository == null || warehouseRepository == null) {
            throw new IllegalArgumentException("AuthorizationService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public boolean canPerform(String userId, BusinessOperation operation) {
        if (userId == null || userId.isBlank() || operation == null) {
            return false;
        }
        return personRepository.findById(userId)
                .map(operation::isAllowedFor)
                .orElse(false);
    }

    @Override
    public void requirePermission(String userId, BusinessOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation must not be null");
        }
        Person person = personRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User '" + userId + "' does not exist"));
        if (!operation.isAllowedFor(person)) {
            throw new InvalidRoleAssignmentException(operation.getDescription(),
                    person.getRole(),
                    operation.getAllowedRoles().stream()
                            .map(SystemRole::getCode)
                            .collect(Collectors.joining(", ")));
        }
    }

    @Override
    public boolean canAccess(String userId, String resourceType, String resourceId) {
        if (userId == null || userId.isBlank()
                || resourceType == null || resourceType.isBlank()
                || resourceId == null || resourceId.isBlank()) {
            return false;
        }
        Person person = personRepository.findById(userId).orElse(null);
        if (person == null || !person.isActive()) {
            return false;
        }
        boolean privileged = person.getRole() == SystemRole.ADMINISTRATOR
                || person.getRole() == SystemRole.SUPERVISOR;
        if (privileged) {
            return true;
        }
        return switch (resourceType.toUpperCase()) {
            case RESOURCE_BUYER, RESOURCE_SELLER -> userId.equals(resourceId);
            case RESOURCE_WAREHOUSE -> canAccessWarehouse(userId, resourceId);
            case RESOURCE_ORDER -> orderRepository.findById(resourceId)
                    .map(order -> order.getBuyer().getIdentifier().equals(userId))
                    .orElse(false);
            default -> false;
        };
    }

    @Override
    public void requireAccess(String userId, String resourceType, String resourceId) {
        if (!canAccess(userId, resourceType, resourceId)) {
            throw new IllegalArgumentException("User '" + userId
                    + "' is not authorized to access " + resourceType + "/" + resourceId);
        }
    }

    private boolean canAccessWarehouse(String userId, String warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElse(null);
        if (warehouse == null) {
            return false;
        }
        if (warehouse instanceof MarketplaceWarehouse) {
            return false; // only privileged roles (already checked)
        }
        return warehouse instanceof SellerWarehouse sellerWarehouse
                && sellerWarehouse.getOwner().getIdentifier().equals(userId);
    }
}
