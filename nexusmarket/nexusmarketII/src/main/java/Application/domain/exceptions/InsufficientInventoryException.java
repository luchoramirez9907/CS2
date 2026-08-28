package Application.domain.exceptions;

/**
 * Thrown when an inventory operation requires more stock than the
 * available quantity. Negative stock is never permitted (per SDD).
 */
public class InsufficientInventoryException extends DomainException {

    private final String productIdentifier;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientInventoryException(String productIdentifier, int requestedQuantity, int availableQuantity) {
        super("Insufficient inventory for product '" + productIdentifier + "': requested="
                + requestedQuantity + ", available=" + availableQuantity);
        this.productIdentifier = productIdentifier;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
