package Application.domain.exceptions;

/**
 * Base exception for all business exceptions of the NexusMarket domain.
 *
 * Business exceptions belong exclusively to the domain (per SDD -
 * Software Architecture).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
