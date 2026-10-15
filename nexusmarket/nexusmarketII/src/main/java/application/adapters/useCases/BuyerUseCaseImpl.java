package application.adapters.useCases;

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
import application.domain.ports.in.BuyerPort;
import application.domain.services.cart.ConsultCartService;
import application.domain.services.billing.ConsultInvoiceService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.logistics.ConsultShipmentService;
import application.domain.services.order.ConfirmOrderService;
import application.domain.services.buyer.ManageBuyerService;
import application.domain.services.cart.ManageCartItemsService;
import application.domain.services.catalog.ManageProductService;
import application.domain.services.returns.ProcessRefundService;
import application.domain.services.returns.RequestReturnService;
import application.domain.services.returns.ResolveReturnService;
import application.domain.valueobjects.Address;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Use Case Adapter implementing BuyerPort. The authenticated User is always
 * the buyer the operations act on; delegates to the Domain Services and contains no business rules.
 */
@Service
public class BuyerUseCaseImpl implements BuyerPort {

    private final ManageBuyerService manageBuyerService;
    private final ManageProductService manageProductService;
    private final ManageCartItemsService manageCartItemsService;
    private final ConsultCartService consultCartService;
    private final ConfirmOrderService confirmOrderService;
    private final ConsultOrderService consultOrderService;
    private final ConsultInvoiceService consultInvoiceService;
    private final ConsultShipmentService consultShipmentService;
    private final RequestReturnService requestReturnService;
    private final ResolveReturnService resolveReturnService;
    private final ProcessRefundService processRefundService;

    public BuyerUseCaseImpl(ManageBuyerService manageBuyerService,
                            ManageProductService manageProductService,
                            ManageCartItemsService manageCartItemsService,
                            ConsultCartService consultCartService,
                            ConfirmOrderService confirmOrderService,
                            ConsultOrderService consultOrderService,
                            ConsultInvoiceService consultInvoiceService,
                            ConsultShipmentService consultShipmentService,
                            RequestReturnService requestReturnService,
                            ResolveReturnService resolveReturnService,
                            ProcessRefundService processRefundService) {
        this.manageBuyerService = manageBuyerService;
        this.manageProductService = manageProductService;
        this.manageCartItemsService = manageCartItemsService;
        this.consultCartService = consultCartService;
        this.confirmOrderService = confirmOrderService;
        this.consultOrderService = consultOrderService;
        this.consultInvoiceService = consultInvoiceService;
        this.consultShipmentService = consultShipmentService;
        this.requestReturnService = requestReturnService;
        this.resolveReturnService = resolveReturnService;
        this.processRefundService = processRefundService;
    }

    // ---------------------------------------------------------------- Profile

    @Override
    public Buyer consultMyProfile(User user) {
        return manageBuyerService.consultBuyer(user.getIdentifier(), user.getIdentifier());
    }

    @Override
    public Buyer updateMyProfile(User user, String fullName, String email, Address primaryAddress,
                                 List<Address> additionalAddresses) {
        return manageBuyerService.updateBuyer(user.getIdentifier(), user.getIdentifier(), fullName, email,
                primaryAddress, additionalAddresses);
    }

    // ---------------------------------------------------------------- Catalog

    @Override
    public Product consultProduct(User user, String productId) {
        return manageProductService.consultProduct(user.getIdentifier(), productId);
    }

    // ---------------------------------------------------------------- Shopping cart

    @Override
    public ShoppingCart addItemToCart(User user, String productId, int quantity) {
        // A null cart id resolves (or creates) the buyer's active cart.
        return manageCartItemsService.addItemToCart(user.getIdentifier(), null, productId, quantity);
    }

    @Override
    public ShoppingCart updateCartItemQuantity(User user, String productId, int quantity) {
        return manageCartItemsService.updateItemQuantity(user.getIdentifier(), productId, quantity);
    }

    @Override
    public ShoppingCart removeCartItem(User user, String productId) {
        return manageCartItemsService.removeItem(user.getIdentifier(), productId);
    }

    @Override
    public ShoppingCart clearCart(User user) {
        return manageCartItemsService.clearCart(user.getIdentifier());
    }

    @Override
    public Optional<ShoppingCart> consultMyCart(User user) {
        return consultCartService.consultCart(user.getIdentifier());
    }

    // ---------------------------------------------------------------- Orders, billing and logistics

    @Override
    public Order checkout(User user) {
        // A null cart id checks out the buyer's active cart.
        return confirmOrderService.checkout(user.getIdentifier(), null);
    }

    @Override
    public Order consultOrder(User user, String orderId) {
        return consultOrderService.consultOrder(user.getIdentifier(), orderId);
    }

    @Override
    public List<Order> consultMyOrders(User user) {
        return manageBuyerService.consultBuyerOrders(user.getIdentifier(), user.getIdentifier());
    }

    @Override
    public Invoice consultInvoice(User user, String orderId) {
        return consultInvoiceService.consultInvoice(user.getIdentifier(), orderId);
    }

    @Override
    public Shipment consultShipment(User user, String orderId) {
        return consultShipmentService.consultShipment(user.getIdentifier(), orderId);
    }

    @Override
    public List<ShipmentTrackingEvent> consultTracking(User user, String orderId) {
        return consultShipmentService.consultTracking(user.getIdentifier(), orderId);
    }

    // ---------------------------------------------------------------- Returns and refunds

    @Override
    public Return requestReturn(User user, String orderId, String reason) {
        return requestReturnService.requestReturn(user.getIdentifier(), orderId, reason);
    }

    @Override
    public Return consultReturn(User user, String returnId) {
        return resolveReturnService.consultReturn(user.getIdentifier(), returnId);
    }

    @Override
    public List<Return> consultMyReturns(User user) {
        return manageBuyerService.consultBuyerReturns(user.getIdentifier(), user.getIdentifier());
    }

    @Override
    public Refund consultRefund(User user, String returnId) {
        return processRefundService.consultRefund(user.getIdentifier(), returnId);
    }

    @Override
    public List<Refund> consultMyRefunds(User user) {
        return manageBuyerService.consultBuyerRefunds(user.getIdentifier(), user.getIdentifier());
    }
}
