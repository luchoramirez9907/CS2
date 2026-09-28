package Application.domain.ports.in;

import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Refund;
import Application.domain.models.Return;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.BuyerCommercialStatus;

import java.util.List;

/**
 * Input port (use case): consults, updates and changes the commercial
 * status of an existing buyer, and retrieves the buyer's orders, returns
 * and refunds according to the access permissions of the requesting user.
 */
public interface ManageBuyerUseCase {

    Buyer consultBuyer(String requesterId, String buyerId);

    /**
     * @param additionalAddresses replaces the additional addresses (null keeps them)
     */
    Buyer updateBuyer(String requesterId, String buyerId, String fullName, String email,
                      Address primaryAddress, List<Address> additionalAddresses);

    Buyer changeCommercialStatus(String requesterId, String buyerId, BuyerCommercialStatus status);

    List<Order> consultBuyerOrders(String requesterId, String buyerId);

    List<Return> consultBuyerReturns(String requesterId, String buyerId);

    List<Refund> consultBuyerRefunds(String requesterId, String buyerId);
}
