package Application.domain.models;

import Application.domain.exceptions.BuyerNotAuthorizedException;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.BuyerCommercialStatus;
import Application.domain.valueobjects.UserStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Buyer
 *
 * Represents a user who acquires products published in the catalog.
 * A buyer never administers information belonging to other buyers,
 * nor manages inventory or seller data.
 */
public class Buyer extends Person {

    private Address primaryAddress;
    private final List<Address> additionalAddresses = new ArrayList<>();
    private BuyerCommercialStatus commercialStatus;
    private ShoppingCart cart;
    private final List<Order> orders = new ArrayList<>();

    public Buyer(String identifier, String fullName, String email, Address primaryAddress,
                 BuyerCommercialStatus commercialStatus, UserStatus status) {
        super(identifier, fullName, email, Application.domain.valueobjects.SystemRole.BUYER, status);
        this.primaryAddress = primaryAddress;
        this.commercialStatus = commercialStatus == null
                ? BuyerCommercialStatus.ACTIVE
                : commercialStatus;
    }

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(Address primaryAddress) {
        if (primaryAddress == null) {
            throw new IllegalArgumentException("Primary address must not be null");
        }
        this.primaryAddress = primaryAddress;
    }

    public List<Address> getAdditionalAddresses() {
        return Collections.unmodifiableList(additionalAddresses);
    }

    public void addAdditionalAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Additional address must not be null");
        }
        this.additionalAddresses.add(address);
    }

    public BuyerCommercialStatus getCommercialStatus() {
        return commercialStatus;
    }

    public void setCommercialStatus(BuyerCommercialStatus commercialStatus) {
        if (commercialStatus == null) {
            throw new IllegalArgumentException("Commercial status must not be null");
        }
        this.commercialStatus = commercialStatus;
    }

    /**
     * Determines whether the buyer is commercially authorized to perform
     * purchases (place new orders).
     */
    public boolean canPurchase() {
        return commercialStatus == BuyerCommercialStatus.ACTIVE;
    }

    /**
     * Ensures the buyer is authorized to transact before a purchasing operation.
     */
    public void requirePurchaseAuthorization() {
        if (!canPurchase()) {
            throw new BuyerNotAuthorizedException(getIdentifier(), commercialStatus);
        }
    }

    public ShoppingCart getCart() {
        return cart;
    }

    public void attachCart(ShoppingCart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart must not be null");
        }
        if (!this.equals(cart.getBuyer())) {
            throw new IllegalArgumentException("Cart does not belong to this buyer");
        }
        this.cart = cart;
    }

    public void detachCart() {
        this.cart = null;
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public void addOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        if (!this.equals(order.getBuyer())) {
            throw new IllegalArgumentException("Order does not belong to this buyer");
        }
        this.orders.add(order);
    }
}
