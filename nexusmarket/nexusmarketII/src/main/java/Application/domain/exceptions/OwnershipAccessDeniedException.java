package Application.domain.exceptions;

/**
 * Thrown when a user attempts to access or operate on information that
 * belongs to a buyer, seller, warehouse or order that the user neither
 * owns nor manages (RG-03).
 */
public class OwnershipAccessDeniedException extends DomainException {

    private final String userIdentifier;
    private final String resource;

    public OwnershipAccessDeniedException(String userIdentifier, String resource) {
        super("User '" + userIdentifier + "' is not authorized to access " + resource);
        this.userIdentifier = userIdentifier;
        this.resource = resource;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public String getResource() {
        return resource;
    }
}
