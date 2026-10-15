package application.domain.exceptions;

/**
 * Thrown when a password does not satisfy the password policy of the
 * platform. The message never includes the rejected password.
 */
public class WeakPasswordException extends DomainException {

    public WeakPasswordException(String reason) {
        super("Password does not satisfy the password policy: " + reason);
    }
}
