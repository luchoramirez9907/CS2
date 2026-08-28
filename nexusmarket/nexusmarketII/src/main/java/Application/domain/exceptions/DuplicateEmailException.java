package Application.domain.exceptions;

/**
 * Thrown when the email (or platform identifier) of a person already
 * exists. Both must be unique across the platform (per SDD).
 */
public class DuplicateEmailException extends DomainException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("A person with email '" + email + "' already exists in the platform");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
