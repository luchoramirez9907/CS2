package application.domain.exceptions;

/**
 * Thrown when a person whose status is not ACTIVE (blocked or inactive)
 * tries to authenticate.
 */
public class AccountDisabledException extends DomainException {

    public AccountDisabledException(String personIdentifier) {
        super("The account of '" + personIdentifier + "' is not active");
    }
}
