package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.ShoppingCart;
import application.domain.ports.out.BuyerRepository;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ShoppingCartRepository;
import application.domain.services.billing.GenerateInvoiceService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ConfirmOrderService
 *
 * Catalog service "Confirm Order": converts the
 * provisional selection of a shopping cart into the formal commercial commitment (Order),
 * generates its invoice, and frees the cart. The order enters the
 * PENDING_PAYMENT state awaiting financial confirmation.
 */
public class ConfirmOrderService {

    private final BuyerRepository buyerRepository;
    private final ShoppingCartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final GenerateInvoiceService generateInvoiceService;
    private final NotificationService notificationService;

    public ConfirmOrderService(BuyerRepository buyerRepository,
                               ShoppingCartRepository cartRepository,
                               OrderRepository orderRepository,
                               GenerateInvoiceService generateInvoiceService,
                               NotificationService notificationService) {
        if (buyerRepository == null || cartRepository == null || orderRepository == null
                || generateInvoiceService == null || notificationService == null) {
            throw new IllegalArgumentException("ConfirmOrderService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.generateInvoiceService = generateInvoiceService;
        this.notificationService = notificationService;
    }

    /**
     * @param buyerId identifier of the Buyer placing the order
     * @param cartId  identifier of the cart to convert (optional; resolved when blank)
     * @return the confirmed Order (status PENDING_PAYMENT)
     */
    public Order checkout(String buyerId, String cartId) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
        buyer.requireActive();
        buyer.requirePurchaseAuthorization();

        ShoppingCart cart = resolveCart(buyer, cartId);
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an empty shopping cart");
        }

        // The stock was already reserved when the items were added to the cart;
        // the order inherits those reservations.
        Order order = Order.fromCart(cart, UUID.randomUUID().toString(), LocalDateTime.now());
        order.confirm();
        generateInvoiceService.issueInvoice(order);

        orderRepository.save(order);
        buyer.addOrder(order);
        buyerRepository.save(buyer);

        cart.clear();
        buyer.detachCart();
        cartRepository.delete(cart);

        notificationService.notify(buyer, "Order confirmed",
                "Order " + order.getOrderId() + " is pending payment confirmation");
        return order;
    }

    private ShoppingCart resolveCart(Buyer buyer, String cartId) {
        if (cartId != null && !cartId.isBlank()) {
            ShoppingCart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new EntityNotFoundException("Cart", cartId));
            if (!buyer.equals(cart.getBuyer())) {
                throw new IllegalArgumentException("Cart does not belong to the buyer");
            }
            return cart;
        }
        return cartRepository.findActiveByBuyerId(buyer.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Buyer has no active shopping cart"));
    }
}
