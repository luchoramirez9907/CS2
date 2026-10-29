package application.domain.services.cart;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.CartItem;
import application.domain.models.Product;
import application.domain.models.ShoppingCart;
import application.domain.ports.out.BuyerRepository;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.ShoppingCartRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.inventory.InventoryReservationService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ManageCartItemsService
 *
 * Catalog service "Manage Cart Items" (add, update
 * quantity, remove, clear). The cart reserves stock at selection time:
 * adding units reserves stock and removing them releases it. Only
 * commercially authorized buyers may add selections of published products,
 * always at the catalog price. A buyer only operates on its own cart.
 */
public class ManageCartItemsService {

    private final BuyerRepository buyerRepository;
    private final ShoppingCartRepository cartRepository;
    private final ProductRepository productRepository;
    private final InventoryReservationService inventoryReservationService;
    private final NotificationService notificationService;

    public ManageCartItemsService(BuyerRepository buyerRepository,
                                  ShoppingCartRepository cartRepository,
                                  ProductRepository productRepository,
                                  InventoryReservationService inventoryReservationService,
                                  NotificationService notificationService) {
        if (buyerRepository == null || cartRepository == null || productRepository == null
                || inventoryReservationService == null || notificationService == null) {
            throw new IllegalArgumentException("ManageCartItemsService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.inventoryReservationService = inventoryReservationService;
        this.notificationService = notificationService;
    }

    /**
     * @param buyerId   identifier of the Buyer
     * @param cartId    identifier of the active cart (optional; resolved when blank)
     * @param productId identifier of the Product to add
     * @param quantity  quantity to add
     * @return the updated ShoppingCart
     */
    public ShoppingCart addItemToCart(String buyerId, String cartId, String productId, int quantity) {
        ServiceValidations.requireText(buyerId, "buyer id");
        ServiceValidations.requireText(productId, "product id");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
        buyer.requireActive();
        buyer.requirePurchaseAuthorization();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Product '" + product.getName()
                    + "' is not available in the catalog");
        }

        // The unit price is captured from the catalog; the buyer never sets it.
        ShoppingCart cart = resolveCart(buyer, cartId);
        if (product.requiresPhysicalDispatch()) {
            inventoryReservationService.reserveStock(product, quantity, buyer);
        }
        // Reserve first: if there is no stock the cart is left untouched.
        cart.addItem(product, quantity, product.getPrice());

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
    public ShoppingCart updateItemQuantity(String buyerId, String productId, int quantity) {
        ServiceValidations.requireText(productId, "product id");
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
    public ShoppingCart removeItem(String buyerId, String productId) {
        ServiceValidations.requireText(productId, "product id");
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

    private void releaseReservation(CartItem item, Buyer buyer) {
        if (item.getProduct().requiresPhysicalDispatch()) {
            inventoryReservationService.releaseReservation(item.getProduct(), item.getQuantity(), buyer);
        }
    }

    private Buyer findOperationalBuyer(String buyerId) {
        ServiceValidations.requireText(buyerId, "buyer id");
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
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
                    .orElseThrow(() -> new EntityNotFoundException("Cart", cartId));
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
}
