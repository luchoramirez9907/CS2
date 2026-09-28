package Application.domain.ports.in;

import Application.domain.models.Buyer;
import Application.domain.valueobjects.Address;

/**
 * Input port (use case): Register Buyer.
 *
 * Creates a new buyer (self-registration) and establishes the buyer's
 * initial delivery information and commercial status.
 */
public interface RegisterBuyerUseCase {

    /**
     * @param identifier     unique platform identifier for the new buyer
     * @param fullName       official full name of the buyer
     * @param email          unique email of the buyer
     * @param primaryAddress initial delivery address of the buyer
     * @return the registered Buyer
     */
    Buyer registerBuyer(String identifier, String fullName, String email, Address primaryAddress);
}
