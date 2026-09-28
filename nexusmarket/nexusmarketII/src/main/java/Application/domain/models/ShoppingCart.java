package Application.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * ShoppingCart
 *
 * Represents the provisional selection of products made by a buyer
 * before confirming an order. A ShoppingCart is converted into one Order
 * upon checkout confirmation.
 */
public class ShoppingCart {

    private final String cartId;
    private final Buyer buyer;
    private final List<CartItem> items = new ArrayList<>();
    private final LocalDateTime createdDate;

    public ShoppingCart(String cartId, Buyer buyer, LocalDateTime createdDate) {
        if (cartId == null || cartId.isBlank()) {
            throw new IllegalArgumentException("Cart id must not be null or blank");
        }
        if (buyer == null) {
            throw new IllegalArgumentException("A ShoppingCart belongs to exactly one Buyer");
        }
        if (createdDate == null) {
            throw new IllegalArgumentException("Cart creation date must not be null");
        }
        this.cartId = cartId;
        this.buyer = buyer;
        this.createdDate = createdDate;
    }

    public String getCartId() {
        return cartId;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Adds a product selection to the cart. If the product is already
     * selected, the quantity is accumulated and the reference price is
     * updated to the latest selection price.
     */
    public void addItem(Product product, int quantity, BigDecimal unitPrice) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be null or negative");
        }
        for (CartItem item : items) {
            if (item.getProduct().equals(product)) {
                item.increaseQuantity(quantity, unitPrice);
                return;
            }
        }
        items.add(new CartItem(product, quantity, unitPrice));
    }

    public Optional<CartItem> findItem(String productId) {
        return items.stream()
                .filter(item -> item.getProduct().getIdentifier().equals(productId))
                .findFirst();
    }

    /**
     * Replaces the quantity selected of a product already in the cart.
     */
    public void updateItemQuantity(String productId, int newQuantity) {
        CartItem item = findItem(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product '" + productId
                        + "' is not in the cart"));
        item.changeQuantity(newQuantity);
    }

    /**
     * Removes the selection of the product identified by the given id.
     */
    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getIdentifier().equals(productId));
    }

    /**
     * Clears every selection. Used when the cart is converted into an Order.
     */
    public void clear() {
        items.clear();
    }

    public BigDecimal total() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
