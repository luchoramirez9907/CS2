package application.domain.exceptions;

/**
 * Thrown when a login attempt fails. The same message is used for an
 * unknown email and a wrong password, so the response never reveals which
 * accounts exist.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
