package Application.domain.models;

import Application.domain.valueobjects.Address;

/**
 * SellerWarehouse
 *
 * Represents a warehouse owned and operated by a Seller. A
 * SellerWarehouse must always be linked to exactly one Seller.
 */
public class SellerWarehouse extends Warehouse {

    private final Seller owner;

    public SellerWarehouse(String identifier, String name, Address address, Seller owner) {
        super(identifier, name, address);
        if (owner == null) {
            throw new IllegalArgumentException("A Seller warehouse must always be linked to exactly one Seller");
        }
        this.owner = owner;
    }

    public Seller getOwner() {
        return owner;
    }
}
