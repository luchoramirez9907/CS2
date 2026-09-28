package Application.domain.ports.in;

import Application.domain.models.Seller;
import Application.domain.valueobjects.UserStatus;

/**
 * Input port (use case): consults, updates and changes the operational
 * status of an existing seller.
 */
public interface ManageSellerUseCase {

    Seller consultSeller(String requesterId, String sellerId);

    Seller updateSeller(String requesterId, String sellerId, String fullName, String email);

    Seller changeSellerStatus(String requesterId, String sellerId, UserStatus status);
}
