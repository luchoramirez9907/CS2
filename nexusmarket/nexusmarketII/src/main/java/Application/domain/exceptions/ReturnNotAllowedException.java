package Application.domain.exceptions;

/**
 * Thrown when a return is requested against an order that has not been
 * delivered. A Return can only be requested against a delivered Order
 * (per SDD - Post-Sale Processes).
 */
public class ReturnNotAllowedException extends DomainException {

    private final String orderId;

    public ReturnNotAllowedException(String orderId, String reason) {
        super("Return is not allowed for order '" + orderId + "': " + reason);
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
