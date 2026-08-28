package Application.domain.models;

import java.math.BigDecimal;

/**
 * OrderItem
 *
 * Represents a confirmed product line within an order, including the
 * quantity and price agreed at checkout.
 */
public class OrderItem {

    private final Product product;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public OrderItem(Product product, int quantity, BigDecimal unitPrice) {
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
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
