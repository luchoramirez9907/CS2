package Application.domain.ports.in;

import Application.domain.models.Seller;

/**
 * Input port (use case): registers a new Seller in the platform.
 * Per business rules, the seller must be incorporated by an Administrator;
 * self-registration is not allowed.
 */
public interface RegisterSellerUseCase {

    /**
     * @param administratorId identifier of the Administrator performing the registration
     * @param identifier      unique platform identifier for the new seller
     * @param fullName        official full name of the seller
     * @param email           unique email of the seller
     * @return the registered Seller
     */
    Seller registerSeller(String administratorId, String identifier, String fullName, String email);
}
