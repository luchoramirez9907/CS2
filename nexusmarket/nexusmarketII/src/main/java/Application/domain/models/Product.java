package Application.domain.models;

import Application.domain.valueobjects.ProductStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Product (Abstract)
 *
 * Represents a good offered through the catalog, published and managed
 * by a Seller. The catalog distinguishes physical products, which require
 * inventory and dispatch, from digital products, which are delivered
 * immediately after payment.
 */
public abstract class Product {

    private final String identifier;
    private String name;
    private String description;
    private final List<ProductVariant> variants = new ArrayList<>();
    private ProductStatus status;
    private final Seller seller;

    protected Product(String identifier, String name, String description, ProductStatus status,
                      Seller seller) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Product identifier must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be null or blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("Product status must not be null");
        }
        if (seller == null) {
            throw new IllegalArgumentException("Product must be published by a Seller");
        }
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.status = status;
        this.seller = seller;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public void addVariant(String name, String value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Variant name must not be null or blank");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Variant value must not be null or blank");
        }
        this.variants.add(new ProductVariant(name, value));
    }

    public ProductStatus getStatus() {
        return status;
    }

    /**
     * Changes the catalog status. DISCONTINUED is permanent: a discontinued
     * product can never return to the catalog.
     */
    public void changeStatus(ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Product status must not be null");
        }
        if (this.status == ProductStatus.DISCONTINUED && status != ProductStatus.DISCONTINUED) {
            throw new IllegalArgumentException("Product '" + identifier
                    + "' is permanently discontinued and cannot change its status");
        }
        this.status = status;
    }

    public boolean isDiscontinued() {
        return status == ProductStatus.DISCONTINUED;
    }

    /**
     * Updates the descriptive information of the product. A discontinued
     * product can no longer be modified.
     */
    public void updateInformation(String name, String description) {
        requireNotDiscontinued();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be null or blank");
        }
        this.name = name;
        this.description = description;
    }

    /**
     * Replaces every variant of the product with the given ones.
     */
    public void replaceVariants(List<ProductVariant> newVariants) {
        requireNotDiscontinued();
        if (newVariants == null) {
            throw new IllegalArgumentException("Variants must not be null");
        }
        List<ProductVariant> replacement = new ArrayList<>(newVariants);
        this.variants.clear();
        for (ProductVariant variant : replacement) {
            addVariant(variant.getName(), variant.getValue());
        }
    }

    private void requireNotDiscontinued() {
        if (isDiscontinued()) {
            throw new IllegalArgumentException("Product '" + identifier
                    + "' is discontinued and cannot be modified");
        }
    }

    /**
     * A product is commercially available when it is published.
     */
    public boolean isAvailable() {
        return status == ProductStatus.PUBLISHED;
    }

    public Seller getSeller() {
        return seller;
    }

    /**
     * Determines whether this product requires inventory management and
     * physical dispatch through a Shipment. Only PhysicalProduct
     * instances do; DigitalProduct instances are delivered immediately
     * after payment confirmation.
     */
    public abstract boolean requiresPhysicalDispatch();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product product = (Product) o;
        return identifier.equals(product.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
