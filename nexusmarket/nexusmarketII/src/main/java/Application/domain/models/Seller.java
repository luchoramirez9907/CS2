package Application.domain.models;

import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Seller
 *
 * Represents a business responsible for publishing and managing products
 * in the catalog. Sellers cannot self-register; they are incorporated
 * into the platform by an Administrator.
 */
public class Seller extends Person {

    private final List<SellerWarehouse> warehouses = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private final Administrator registeredBy;

    public Seller(String identifier, String fullName, String email, Administrator registeredBy,
                  UserStatus status) {
        super(identifier, fullName, email, SystemRole.SELLER, status);
        if (registeredBy == null) {
            throw new InvalidRoleAssignmentException("register a Seller",
                    SystemRole.SELLER, SystemRole.ADMINISTRATOR);
        }
        this.registeredBy = registeredBy;
    }

    /**
     * Administrator who incorporated the seller into the platform.
     */
    public Administrator getRegisteredBy() {
        return registeredBy;
    }

    public List<SellerWarehouse> getWarehouses() {
        return Collections.unmodifiableList(warehouses);
    }

    public void addWarehouse(SellerWarehouse warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("Warehouse must not be null");
        }
        if (!this.equals(warehouse.getOwner())) {
            throw new IllegalArgumentException("Warehouse does not belong to this seller");
        }
        this.warehouses.add(warehouse);
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (!this.equals(product.getSeller())) {
            throw new IllegalArgumentException("Product is not owned by this seller");
        }
        this.products.add(product);
    }
}
