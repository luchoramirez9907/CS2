package Application.domain.exceptions;

import Application.domain.valueobjects.BuyerCommercialStatus;

/**
 * Thrown when a buyer is not commercially authorized to perform a
 * purchasing operation (e.g. blocked or restricted buyer).
 */
public class BuyerNotAuthorizedException extends DomainException {

    private final String buyerIdentifier;
    private final BuyerCommercialStatus commercialStatus;

    public BuyerNotAuthorizedException(String buyerIdentifier, BuyerCommercialStatus commercialStatus) {
        super("Buyer '" + buyerIdentifier + "' is not authorized to perform purchasing operations"
                + " (commercial status: " + commercialStatus.getCode() + ")");
        this.buyerIdentifier = buyerIdentifier;
        this.commercialStatus = commercialStatus;
    }

    public String getBuyerIdentifier() {
        return buyerIdentifier;
    }

    public BuyerCommercialStatus getCommercialStatus() {
        return commercialStatus;
    }
}
