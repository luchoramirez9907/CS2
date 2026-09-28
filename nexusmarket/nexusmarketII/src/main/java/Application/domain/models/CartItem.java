package Application.domain.models;

import java.math.BigDecimal;

/**
 * CartItem
 *
 * Represents a product selection within a shopping cart, together with
 * its quantity and reference price.
 */
public class CartItem {

    private final Product product;
    private int quantity;
    private BigDecimal unitPrice;

    public CartItem(Product product, int quantity, BigDecimal unitPrice) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be null or negative");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    void increaseQuantity(int amount, BigDecimal newUnitPrice) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Quantity increment must be positive");
        }
        if (newUnitPrice == null || newUnitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be null or negative");
        }
        this.quantity += amount;
        this.unitPrice = newUnitPrice;
    }

    void changeQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
