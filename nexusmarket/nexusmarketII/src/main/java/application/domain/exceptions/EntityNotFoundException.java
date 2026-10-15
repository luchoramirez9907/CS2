package application.domain.exceptions;

/**
 * Thrown when a referenced business entity (person, product, order,
 * warehouse, return, etc.) does not exist.
 */
public class EntityNotFoundException extends DomainException {

    private final String entityType;
    private final String identifier;

    public EntityNotFoundException(String entityType, String identifier) {
        super(entityType + " '" + identifier + "' does not exist");
        this.entityType = entityType;
        this.identifier = identifier;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getIdentifier() {
        return identifier;
    }
}
