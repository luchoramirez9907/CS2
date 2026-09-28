package Application.domain.ports.in;

import Application.domain.models.Seller;

/**
 * Input port (use case): Manage Seller.
 *
 * Consults, updates, and changes the operational status of an existing
 * seller according to the applicable business rules.
 */
public interface ManageSellerUseCase {

    Seller consultSeller(String requesterId, String sellerId);

    /**
     * @param performerId identifier of the Administrator performing the change
     * @param sellerId    identifier of the seller whose status changes
     * @param action      one of ACTIVE, INACTIVE or BLOCKED
     * @return the updated Seller
     */
    Seller changeSellerStatus(String performerId, String sellerId, String action);
}
