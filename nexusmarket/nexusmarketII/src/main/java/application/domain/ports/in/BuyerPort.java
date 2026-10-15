package application.domain.ports.in;

import application.domain.models.Buyer;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Shipment;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
import application.domain.valueobjects.Address;

import java.util.List;
import java.util.Optional;

/**
 * Role Input Port (BUYER): operations a buyer performs over its own
 * profile, shopping cart, orders, returns and refunds.
 *
 * Every method receives the authenticated User; the buyer always operates
 * on its own information (RG-03).
 */
public interface BuyerPort {

    // Profile
    Buyer consultMyProfile(User user);

    Buyer updateMyProfile(User user, String fullName, String email, Address primaryAddress,
                          List<Address> additionalAddresses);

    // Catalog
    Product consultProduct(User user, String productId);

    // Shopping cart
    ShoppingCart addItemToCart(User user, String productId, int quantity);

    ShoppingCart updateCartItemQuantity(User user, String productId, int quantity);

    ShoppingCart removeCartItem(User user, String productId);

    ShoppingCart clearCart(User user);

    Optional<ShoppingCart> consultMyCart(User user);

    // Orders, billing and logistics
    Order checkout(User user);

    Order consultOrder(User user, String orderId);

    List<Order> consultMyOrders(User user);

    Invoice consultInvoice(User user, String orderId);

    Shipment consultShipment(User user, String orderId);

    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);

    // Returns and refunds
    Return requestReturn(User user, String orderId, String reason);

    Return consultReturn(User user, String returnId);

    List<Return> consultMyReturns(User user);

    Refund consultRefund(User user, String returnId);

    List<Refund> consultMyRefunds(User user);
}
