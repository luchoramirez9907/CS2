package Application.domain.ports.in;

/**
 * Input port (use case): Validate Ownership Access.
 *
 * Determines whether a user is authorized to access or operate on
 * information belonging to a specific buyer, seller, warehouse or order
 * that it owns or manages.
 */
public interface ValidateOwnershipAccessUseCase {

    String RESOURCE_BUYER = "BUYER";
    String RESOURCE_SELLER = "SELLER";
    String RESOURCE_WAREHOUSE = "WAREHOUSE";
    String RESOURCE_ORDER = "ORDER";

    boolean canAccess(String userId, String resourceType, String resourceId);

    /** @throws IllegalArgumentException when access is not authorized */
    void requireAccess(String userId, String resourceType, String resourceId);
}
