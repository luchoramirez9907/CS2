package Application.domain.enums;

import Application.domain.models.Person;
import Application.domain.valueobjects.SystemRole;

import java.util.Set;

/**
 * BusinessOperation
 *
 * Controlled catalog of the significant business operations of the
 * NexusMarket system, together with the roles authorized to perform
 * them (RG-02 / RG-03). Used by the Authorization services to validate
 * permissions based on the user's role and status.
 */
public enum BusinessOperation {

    REGISTER_USER("Register a system participant", Set.of(SystemRole.ADMINISTRATOR)),
    MANAGE_USER("Consult, update or change the status of a user", Set.of(SystemRole.ADMINISTRATOR)),
    REGISTER_SELLER("Incorporate a seller into the platform", Set.of(SystemRole.ADMINISTRATOR)),
    MANAGE_SELLER("Consult, update or change the status of a seller", Set.of(SystemRole.ADMINISTRATOR)),
    MANAGE_BUYER("Consult or change the commercial status of a buyer", Set.of(SystemRole.ADMINISTRATOR)),
    REGISTER_MARKETPLACE_WAREHOUSE("Register a warehouse owned by the Marketplace", Set.of(SystemRole.ADMINISTRATOR)),
    REGISTER_SELLER_WAREHOUSE("Register a warehouse owned by a seller", Set.of(SystemRole.SELLER)),
    MANAGE_WAREHOUSE("Update warehouse information", Set.of(SystemRole.ADMINISTRATOR, SystemRole.SELLER)),
    PUBLISH_PRODUCT("Publish a product in the catalog", Set.of(SystemRole.SELLER)),
    MANAGE_PRODUCT("Consult or update a published product", Set.of(SystemRole.SELLER)),
    CHANGE_PRODUCT_STATUS("Suspend or discontinue a product", Set.of(SystemRole.SELLER)),
    REGISTER_INVENTORY_MOVEMENT("Record stock-in, adjustments or return reinstatements", Set.of(SystemRole.LOGISTICS_OPERATOR)),
    MANAGE_CART("Add, update, remove or clear cart selections", Set.of(SystemRole.BUYER)),
    CONFIRM_ORDER("Convert a shopping cart into a formal order", Set.of(SystemRole.BUYER)),
    CONFIRM_PAYMENT("Register the financial confirmation of an order", Set.of(SystemRole.BUYER, SystemRole.ADMINISTRATOR)),
    GENERATE_INVOICE("Generate the billing record of a confirmed order", Set.of(SystemRole.ADMINISTRATOR)),
    MANAGE_SHIPMENTS("Create, dispatch or deliver shipments", Set.of(SystemRole.LOGISTICS_OPERATOR)),
    REQUEST_RETURN("Request a return over a delivered order", Set.of(SystemRole.BUYER)),
    RESOLVE_RETURN("Approve or reject a return request", Set.of(SystemRole.ADMINISTRATOR)),
    PROCESS_REFUND("Process the reimbursement of an approved return", Set.of(SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR)),
    CONSULT_REPORTS("Consult administrative and performance reports", Set.of(SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR));

    private final String description;
    private final Set<SystemRole> allowedRoles;

    BusinessOperation(String description, Set<SystemRole> allowedRoles) {
        this.description = description;
        this.allowedRoles = allowedRoles;
    }

    public String getDescription() {
        return description;
    }

    public Set<SystemRole> getAllowedRoles() {
        return allowedRoles;
    }

    /**
     * Determines whether the given person holds the role and operational
     * status required to perform this operation (Validate Permissions).
     */
    public boolean isAllowedFor(Person person) {
        return person != null
                && person.isActive()
                && allowedRoles.contains(person.getRole());
    }
}
