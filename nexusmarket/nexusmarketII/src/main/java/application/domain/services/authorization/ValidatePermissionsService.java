package application.domain.services.authorization;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidRoleAssignmentException;
import application.domain.models.Person;
import application.domain.ports.out.PersonRepository;
import application.domain.valueobjects.SystemRole;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ValidatePermissionsService
 *
 * Catalog service "Validate Permissions". Holds the permission matrix
 * (business operation -> roles allowed) and decides whether a person may
 * perform an operation according to its role and status, ensuring no
 * participant operates outside the boundaries of its assigned role
 * (RG-02 / RG-03). Supervisors are read-only: they are only granted
 * consultations.
 */
public class ValidatePermissionsService {

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

        permissions.put(BusinessOperation.PROCESS_REFUND, Set.of(ADMIN));
        permissions.put(BusinessOperation.CONSULT_REFUND, Set.of(ADMIN, SUPERVISOR, BUYER));

        permissions.put(BusinessOperation.CONSULT_COMMERCIAL_REPORT, Set.of(ADMIN, SUPERVISOR));
        permissions.put(BusinessOperation.CONSULT_SELLER_PERFORMANCE_REPORT, Set.of(ADMIN, SUPERVISOR, SELLER));
        PERMISSIONS = Collections.unmodifiableMap(permissions);
    }

    private final PersonRepository personRepository;

    public ValidatePermissionsService(PersonRepository personRepository) {
        if (personRepository == null) {
            throw new IllegalArgumentException("ValidatePermissionsService requires its dependencies");
        }
        this.personRepository = personRepository;
    }

    public boolean hasPermission(Person user, BusinessOperation operation) {
        return user != null && operation != null && user.isActive()
                && allowedRoles(operation).contains(user.getRole());
    }

    /**
     * Loads the user, and ensures it is active and its role allows the operation.
     *
     * @return the authorized user
     */
    public Person requirePermission(String userId, BusinessOperation operation) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Requesting user id must not be null or blank");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Business operation must not be null");
        }
        Person user = personRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
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
}
