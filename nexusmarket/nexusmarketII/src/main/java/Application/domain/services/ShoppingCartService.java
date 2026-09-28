package Application.domain.services;

import Application.domain.models.Buyer;
import Application.domain.models.CartItem;
import Application.domain.models.Product;
import Application.domain.models.ShoppingCart;
import Application.domain.ports.in.ConsultCartUseCase;
import Application.domain.ports.in.ManageCartItemsUseCase;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.ShoppingCartRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * ShoppingCartService
 *
 * Implements ManageCartItemsUseCase (add, update quantity, remove, clear)
 * and ConsultCartUseCase. The cart reserves stock at selection time:
 * reservedQuantity reflects stock reserved by active shopping carts or
 * orders, and removing selections releases it. Only commercially
 * authorized buyers may add selections of published products. A buyer
 * only operates on its own cart.
 */
public class ShoppingCartService implements ManageCartItemsUseCase, ConsultCartUseCase {

    private final BuyerRepository buyerRepository;
    private final ShoppingCartRepository cartRepository;
    private final ProductRepository productRepository;
    private final InventoryReservationService inventoryReservationService;
    private final NotificationService notificationService;

    public ShoppingCartService(BuyerRepository buyerRepository,
                               ShoppingCartRepository cartRepository,
                               ProductRepository productRepository,
                               InventoryReservationService inventoryReservationService,
                               NotificationService notificationService) {
        if (buyerRepository == null || cartRepository == null || productRepository == null
                || inventoryReservationService == null || notificationService == null) {
            throw new IllegalArgumentException("ShoppingCartService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.inventoryReservationService = inventoryReservationService;
        this.notificationService = notificationService;
    }

    @Override
    public ShoppingCart addItemToCart(String buyerId, String cartId, String productId,
                                      int quantity, BigDecimal unitPrice) {
        requireText(buyerId, "buyer id");
        requireText(productId, "product id");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be null or negative");
        }

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer '" + buyerId + "' does not exist"));
        buyer.requireActive();
        buyer.requirePurchaseAuthorization();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product '" + productId + "' does not exist"));
        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Product '" + product.getName()
                    + "' is not available in the catalog");
        }

        ShoppingCart cart = resolveCart(buyer, cartId);
        cart.addItem(product, quantity, unitPrice);

        if (product.requiresPhysicalDispatch()) {
            inventoryReservationService.reserveStock(product, quantity, buyer);
        }

        buyer.attachCart(cart);
        cartRepository.save(cart);
        notificationService.notify(buyer, "Cart updated",
                quantity + " unit(s) of '" + product.getName() + "' were added to your cart");
        return cart;
    }

    /**
     * Replaces the quantity of a product already in the cart, reserving or
     * releasing the stock difference.
     */
    @Override
    public ShoppingCart updateItemQuantity(String buyerId, String productId, int quantity) {
        requireText(productId, "product id");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive (use remove to delete the item)");
        }
        Buyer buyer = findOperationalBuyer(buyerId);
        ShoppingCart cart = findActiveCart(buyer);
        CartItem item = cart.findItem(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product '" + productId
                        + "' is not in the cart"));

        int delta = quantity - item.getQuantity();
        Product product = item.getProduct();
        if (delta > 0 && !product.isAvailable()) {
            throw new IllegalArgumentException("Product '" + product.getName()
                    + "' is not available in the catalog");
        }
        if (product.requiresPhysicalDispatch()) {
            if (delta > 0) {
                inventoryReservationService.reserveStock(product, delta, buyer);
            } else if (delta < 0) {
                inventoryReservationService.releaseReservation(product, -delta, buyer);
            }
        }
        cart.updateItemQuantity(productId, quantity);
        cartRepository.save(cart);
        return cart;
    }

    /**
     * Removes a product from the cart, releasing its reserved stock.
     */
    @Override
    public ShoppingCart removeItem(String buyerId, String productId) {
        requireText(productId, "product id");
        Buyer buyer = findOperationalBuyer(buyerId);
        ShoppingCart cart = findActiveCart(buyer);
        CartItem item = cart.findItem(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product '" + productId
                        + "' is not in the cart"));
        releaseReservation(item, buyer);
        cart.removeItem(productId);
        cartRepository.save(cart);
        return cart;
    }

    /**
     * Removes every product from the cart, releasing all reserved stock.
     */
    @Override
    public ShoppingCart clearCart(String buyerId) {
        Buyer buyer = findOperationalBuyer(buyerId);
        ShoppingCart cart = findActiveCart(buyer);
        for (CartItem item : cart.getItems()) {
            releaseReservation(item, buyer);
        }
        cart.clear();
        cartRepository.save(cart);
        return cart;
    }

    @Override
    public Optional<ShoppingCart> consultCart(String buyerId) {
        requireText(buyerId, "buyer id");
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer '" + buyerId + "' does not exist"));
        buyer.requireActive();
        return cartRepository.findActiveByBuyerId(buyer.getIdentifier());
    }

    private void releaseReservation(CartItem item, Buyer buyer) {
        if (item.getProduct().requiresPhysicalDispatch()) {
            inventoryReservationService.releaseReservation(item.getProduct(), item.getQuantity(), buyer);
        }
    }

    private Buyer findOperationalBuyer(String buyerId) {
        requireText(buyerId, "buyer id");
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer '" + buyerId + "' does not exist"));
        buyer.requireActive();
        return buyer;
    }

    private ShoppingCart findActiveCart(Buyer buyer) {
        return cartRepository.findActiveByBuyerId(buyer.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("Buyer has no active shopping cart"));
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
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart(
                            UUID.randomUUID().toString(), buyer, LocalDateTime.now());
                    buyer.attachCart(newCart);
                    return newCart;
                });
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
