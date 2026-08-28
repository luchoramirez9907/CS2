package Application.domain.exceptions;

/**
 * Thrown when a Seller attempts to self-register. Sellers cannot
 * self-register; they are incorporated into the platform by an
 * Administrator (per SDD).
 */
public class SellerSelfRegistrationNotAllowedException extends DomainException {

    public SellerSelfRegistrationNotAllowedException(String sellerEmail) {
        super("Seller self-registration is not allowed for '" + sellerEmail
                + "': sellers are incorporated by an Administrator");
    }
}
