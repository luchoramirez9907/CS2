package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.exceptions.OwnershipAccessDeniedException;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.Seller;
import Application.domain.models.SellerWarehouse;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ValidateOwnershipAccessUseCase;
import Application.domain.ports.in.ValidatePermissionsUseCase;
import Application.domain.ports.out.PersonRepository;
import Application.domain.valueobjects.SystemRole;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AuthorizationService
 *
 * Implements ValidatePermissionsUseCase and ValidateOwnershipAccessUseCase.
 * Holds the permission matrix (operation -> roles allowed) and the
 * ownership rules, ensuring no participant operates outside the
 * boundaries of its assigned role (RG-02 / RG-03):
 *
 * - Administrators may access every entity.
 * - Supervisors are read-only: they may consult every entity.
 * - Buyers only access their own information and orders.
 * - Sellers only access their own information, products and warehouses,
 *   and the orders that contain their products.
 * - Logistics operators access warehouses and the orders whose shipment
 *   is assigned to them.
 */
public class AuthorizationService implements ValidatePermissionsUseCase, ValidateOwnershipAccessUseCase {

    private static final SystemRole ADMIN = SystemRole.ADMINISTRATOR;
    private static final SystemRole SUPERVISOR = SystemRole.SUPERVISOR;
    private static final SystemRole BUYER = SystemRole.BUYER;
    private static final SystemRole SELLER = SystemRole.SELLER;
    private static final SystemRole OPERATOR = SystemRole.LOGISTICS_OPERATOR;

    private static final Map<BusinessOperation, Set<SystemRole>> PERMISSIONS;

    static {
        Map<BusinessOperation, Set<SystemRole>> permissions = new EnumMap<>(BusinessOperation.class);
        permissions.put(BusinessOperation.REGISTER_USER, Set.of(ADMIN));
        permissions.put(BusinessOperation.CONSULT_USER, Set.of(ADMIN, SUPERVISOR, BUYER, SELLER, OPERATOR));
        permissions.put(BusinessOperation.UPDATE_USER, Set.of(ADMIN, BUYER, SELLER, OPERATOR));
        permissions.put(BusinessOperation.CHANGE_USER_STATUS, Set.of(ADMIN));

        permissions.put(BusinessOperation.REGISTER_SELLER, Set.of(ADMIN));
        permissions.put(BusinessOperation.CONSULT_SELLER, Set.of(ADMIN, SUPERVISOR, SELLER));
        permissions.put(BusinessOperation.UPDATE_SELLER, Set.of(ADMIN, SELLER));
        permissions.put(BusinessOperation.CHANGE_SELLER_STATUS, Set.of(ADMIN));

        permissions.put(BusinessOperation.REGISTER_BUYER, Set.of(ADMIN));
        permissions.put(BusinessOperation.CONSULT_BUYER, Set.of(ADMIN, SUPERVISOR, BUYER));
        permissions.put(BusinessOperation.UPDATE_BUYER, Set.of(ADMIN, BUYER));
        permissions.put(BusinessOperation.CHANGE_BUYER_COMMERCIAL_STATUS, Set.of(ADMIN));

        permissions.put(BusinessOperation.REGISTER_WAREHOUSE, Set.of(ADMIN, SELLER));
        permissions.put(BusinessOperation.CONSULT_WAREHOUSE, Set.of(ADMIN, SUPERVISOR, SELLER, OPERATOR));
        permissions.put(BusinessOperation.UPDATE_WAREHOUSE, Set.of(ADMIN, SELLER));

        permissions.put(BusinessOperation.PUBLISH_PRODUCT, Set.of(SELLER));
        permissions.put(BusinessOperation.CONSULT_PRODUCT, Set.of(ADMIN, SUPERVISOR, BUYER, SELLER, OPERATOR));
        permissions.put(BusinessOperation.UPDATE_PRODUCT, Set.of(SELLER));
        permissions.put(BusinessOperation.CHANGE_PRODUCT_STATUS, Set.of(ADMIN, SELLER));

        permissions.put(BusinessOperation.REGISTER_INVENTORY_MOVEMENT, Set.of(ADMIN, SELLER, OPERATOR));
        permissions.put(BusinessOperation.CONSULT_INVENTORY, Set.of(ADMIN, SUPERVISOR, SELLER, OPERATOR));

        permissions.put(BusinessOperation.MANAGE_CART, Set.of(BUYER));
        permissions.put(BusinessOperation.CONSULT_CART, Set.of(BUYER));

        permissions.put(BusinessOperation.CONFIRM_ORDER, Set.of(BUYER));
        permissions.put(BusinessOperation.CONFIRM_ORDER_PAYMENT, Set.of(ADMIN));
        permissions.put(BusinessOperation.FINALIZE_ORDER, Set.of(ADMIN, OPERATOR));
        permissions.put(BusinessOperation.CONSULT_ORDER, Set.of(ADMIN, SUPERVISOR, BUYER, SELLER, OPERATOR));

        permissions.put(BusinessOperation.GENERATE_INVOICE, Set.of(ADMIN));
        permissions.put(BusinessOperation.CONSULT_INVOICE, Set.of(ADMIN, SUPERVISOR, BUYER, SELLER));

        permissions.put(BusinessOperation.CREATE_SHIPMENT, Set.of(ADMIN, OPERATOR));
        permissions.put(BusinessOperation.UPDATE_SHIPMENT_STATUS, Set.of(ADMIN, OPERATOR));
        permissions.put(BusinessOperation.CONSULT_SHIPMENT, Set.of(ADMIN, SUPERVISOR, BUYER, SELLER, OPERATOR));

        permissions.put(BusinessOperation.REQUEST_RETURN, Set.of(BUYER));
        permissions.put(BusinessOperation.RESOLVE_RETURN, Set.of(ADMIN));
        permissions.put(BusinessOperation.CONSULT_RETURN, Set.of(ADMIN, SUPERVISOR, BUYER, SELLER));

        permissions.put(BusinessOperation.PROCESS_REFUND, Set.of(ADMIN, SUPERVISOR));
        permissions.put(BusinessOperation.CONSULT_REFUND, Set.of(ADMIN, SUPERVISOR, BUYER));

        permissions.put(BusinessOperation.CONSULT_COMMERCIAL_REPORT, Set.of(ADMIN, SUPERVISOR));
        permissions.put(BusinessOperation.CONSULT_SELLER_PERFORMANCE_REPORT, Set.of(ADMIN, SUPERVISOR, SELLER));
        PERMISSIONS = Collections.unmodifiableMap(permissions);
    }

    private final PersonRepository personRepository;

    public AuthorizationService(PersonRepository personRepository) {
        if (personRepository == null) {
            throw new IllegalArgumentException("AuthorizationService requires its dependencies");
        }
        this.personRepository = personRepository;
    }

    // ------------------------------------------------------------------
    // Validate Permissions
    // ------------------------------------------------------------------

    @Override
    public boolean hasPermission(Person user, BusinessOperation operation) {
        return user != null && operation != null && user.isActive()
                && allowedRoles(operation).contains(user.getRole());
    }

    @Override
    public Person requirePermission(String userId, BusinessOperation operation) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Requesting user id must not be null or blank");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Business operation must not be null");
        }
        Person user = personRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User '" + userId + "' does not exist"));
        user.requireActive();
        Set<SystemRole> allowed = allowedRoles(operation);
        if (!allowed.contains(user.getRole())) {
            throw new InvalidRoleAssignmentException(operation.getDescription(), user.getRole(),
                    describe(allowed));
        }
        return user;
    }

    private Set<SystemRole> allowedRoles(BusinessOperation operation) {
        return PERMISSIONS.getOrDefault(operation, Set.of());
    }

    private String describe(Set<SystemRole> roles) {
        List<String> codes = roles.stream().map(SystemRole::getCode).sorted().toList();
        return "one of " + codes.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    // ------------------------------------------------------------------
    // Validate Ownership Access
    // ------------------------------------------------------------------

    @Override
    public boolean canAccessPerson(Person user, Person target) {
        if (user == null || target == null) {
            return false;
        }
        return isAdministrativeRole(user) || sameIdentity(user, target);
    }

    @Override
    public boolean canAccessBuyer(Person user, Buyer buyer) {
        return canAccessPerson(user, buyer);
    }

    @Override
    public boolean canAccessSeller(Person user, Seller seller) {
        return canAccessPerson(user, seller);
    }

    @Override
    public boolean canAccessWarehouse(Person user, Warehouse warehouse) {
        if (user == null || warehouse == null) {
            return false;
        }
        if (isAdministrativeRole(user) || user.getRole() == OPERATOR) {
            return true;
        }
        if (user.getRole() == SELLER) {
            return warehouse instanceof SellerWarehouse sellerWarehouse
                    && sameIdentity(user, sellerWarehouse.getOwner());
        }
        return false;
    }

    @Override
    public boolean canAccessProduct(Person user, Product product) {
        if (user == null || product == null) {
            return false;
        }
        return isAdministrativeRole(user) || sameIdentity(user, product.getSeller());
    }

    @Override
    public boolean canAccessOrder(Person user, Order order) {
        if (user == null || order == null) {
            return false;
        }
        if (isAdministrativeRole(user)) {
            return true;
        }
        if (user.getRole() == BUYER) {
            return sameIdentity(user, order.getBuyer());
        }
        if (user.getRole() == SELLER) {
            return order.containsProductsOf(user.getIdentifier());
        }
        if (user.getRole() == OPERATOR) {
            return order.getShipment() != null
                    && sameIdentity(user, order.getShipment().getLogisticsOperator());
        }
        return false;
    }

    @Override
    public void requirePersonAccess(Person user, Person target) {
        if (!canAccessPerson(user, target)) {
            throw denied(user, "user '" + identifierOf(target) + "'");
        }
    }

    @Override
    public void requireBuyerAccess(Person user, Buyer buyer) {
        if (!canAccessBuyer(user, buyer)) {
            throw denied(user, "buyer '" + identifierOf(buyer) + "'");
        }
    }

    @Override
    public void requireSellerAccess(Person user, Seller seller) {
        if (!canAccessSeller(user, seller)) {
            throw denied(user, "seller '" + identifierOf(seller) + "'");
        }
    }

    @Override
    public void requireWarehouseAccess(Person user, Warehouse warehouse) {
        if (!canAccessWarehouse(user, warehouse)) {
            throw denied(user, "warehouse '" + (warehouse == null ? null : warehouse.getIdentifier()) + "'");
        }
    }

    @Override
    public void requireProductAccess(Person user, Product product) {
        if (!canAccessProduct(user, product)) {
            throw denied(user, "product '" + (product == null ? null : product.getIdentifier()) + "'");
        }
    }

    @Override
    public void requireOrderAccess(Person user, Order order) {
        if (!canAccessOrder(user, order)) {
            throw denied(user, "order '" + (order == null ? null : order.getOrderId()) + "'");
        }
    }

    private boolean isAdministrativeRole(Person user) {
        return user.getRole() == ADMIN || user.getRole() == SUPERVISOR;
    }

    private boolean sameIdentity(Person first, Person second) {
        return first != null && second != null
                && first.getIdentifier().equals(second.getIdentifier());
    }

    private String identifierOf(Person person) {
        return person == null ? null : person.getIdentifier();
    }

    private OwnershipAccessDeniedException denied(Person user, String resource) {
        return new OwnershipAccessDeniedException(identifierOf(user), resource);
    }
}
