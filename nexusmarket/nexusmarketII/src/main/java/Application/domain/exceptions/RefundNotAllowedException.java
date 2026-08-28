package Application.domain.exceptions;

/**
 * Thrown when a refund is attempted from a return that has not been
 * approved. A Refund can only be generated from an approved Return
 * (per SDD - Post-Sale Processes).
 */
public class RefundNotAllowedException extends DomainException {

    private final String returnId;

    public RefundNotAllowedException(String returnId, String reason) {
        super("Refund is not allowed for return '" + returnId + "': " + reason);
        this.returnId = returnId;
    }

    public String getReturnId() {
        return returnId;
    }
}
