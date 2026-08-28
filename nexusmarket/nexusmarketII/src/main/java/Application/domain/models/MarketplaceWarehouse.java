package Application.domain.models;

import Application.domain.valueobjects.Address;

/**
 * MarketplaceWarehouse
 *
 * Represents a warehouse owned and operated directly by the Marketplace,
 * managed by an Administrator.
 */
public class MarketplaceWarehouse extends Warehouse {

    private final Administrator managedBy;

    public MarketplaceWarehouse(String identifier, String name, Address address, Administrator managedBy) {
        super(identifier, name, address);
        if (managedBy == null) {
            throw new IllegalArgumentException("A Marketplace warehouse must be managed by an Administrator");
        }
        this.managedBy = managedBy;
    }

    public Administrator getManagedBy() {
        return managedBy;
    }
}
