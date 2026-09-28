package Application.domain.ports.in;

import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Return;

import java.util.List;

/**
 * Input port (use case): Manage Buyer.
 *
 * Consults and changes the commercial status of an existing buyer, and
 * retrieves the buyer's associated orders, returns and refunds according
 * to the access permissions of the requesting user.
 */
public interface ManageBuyerUseCase {

    /**
     * Consolidated activity of a buyer: orders and their post-sale
     * returns (each return carries its refund when processed).
     */
    record BuyerActivity(Buyer buyer, List<Order> orders, List<Return> returns) {
    }

    Buyer consultBuyer(String requesterId, String buyerId);

    /**
     * @param performerId identifier of the Administrator performing the change
     * @param buyerId     identifier of the buyer whose commercial status changes
     * @param statusCode  one of ACTIVE, RESTRICTED or BLOCKED
     * @return the updated Buyer
     */
    Buyer changeCommercialStatus(String performerId, String buyerId, String statusCode);

    /**
     * @param requesterId identifier of the requesting user
     * @param buyerId     identifier of the buyer whose activity is retrieved
     * @return the buyer's orders, returns and refunds
     */
    BuyerActivity consultBuyerActivity(String requesterId, String buyerId);
}
