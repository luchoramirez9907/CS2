package application.domain.exceptions;

/**
 * Thrown when a catalog resource (product, warehouse) is registered with an
 * identifier that already exists. Identifiers must be unique (per SDD).
 */
public class DuplicateResourceException extends DomainException {

    private final String resourceType;
    private final String identifier;

    public DuplicateResourceException(String resourceType, String identifier) {
        super("A " + resourceType + " with identifier '" + identifier + "' already exists");
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getIdentifier() {
        return identifier;
    }
}
