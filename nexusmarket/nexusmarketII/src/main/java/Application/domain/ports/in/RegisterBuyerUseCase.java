package Application.domain.ports.in;

import Application.domain.models.Buyer;
import Application.domain.valueobjects.Address;

/**
 * Input port (use case): creates a new buyer with its initial delivery
 * information and commercial status (ACTIVE).
 */
public interface RegisterBuyerUseCase {

    /**
     * @param performerId    identifier of the Administrator registering the buyer;
     *                       null or blank when the buyer registers itself
     * @param identifier     unique platform identifier of the buyer
     * @param fullName       full name of the buyer
     * @param email          unique email of the buyer
     * @param primaryAddress primary delivery address
     * @return the registered Buyer
     */
    Buyer registerBuyer(String performerId, String identifier, String fullName, String email,
                        Address primaryAddress);
}
