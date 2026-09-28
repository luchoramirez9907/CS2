package Application.domain.services;

import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.ShoppingCart;
import Application.domain.ports.in.CreateOrderUseCase;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.ShoppingCartRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OrderCheckoutService
 *
 * Implements the CreateOrderUseCase: converts the provisional selection
 * of a shopping cart into the formal commercial commitment (Order),
 * generates its invoice, and frees the cart. The order enters the
 * PENDING_PAYMENT state awaiting financial confirmation.
 */
public class OrderCheckoutService implements CreateOrderUseCase {

    private final BuyerRepository buyerRepository;
    private final ShoppingCartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final BillingService billingService;
    private final NotificationService notificationService;

    public OrderCheckoutService(BuyerRepository buyerRepository,
                                ShoppingCartRepository cartRepository,
                                OrderRepository orderRepository,
                                BillingService billingService,
                                NotificationService notificationService) {
        if (buyerRepository == null || cartRepository == null || orderRepository == null
                || billingService == null || notificationService == null) {
            throw new IllegalArgumentException("OrderCheckoutService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.billingService = billingService;
        this.notificationService = notificationService;
    }

    @Override
    public Order checkout(String buyerId, String cartId) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer '" + buyerId + "' does not exist"));
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
        billingService.issueInvoice(order);

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
                    .orElseThrow(() -> new IllegalArgumentException("Cart '" + cartId + "' does not exist"));
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
