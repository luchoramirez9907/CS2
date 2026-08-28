package Application.domain.models;

import java.util.Objects;

/**
 * ProductVariant
 *
 * Represents a specific variation of a product, such as a difference in
 * color, size, or model.
 */
public class ProductVariant {

    private final String name;
    private final String value;

    public ProductVariant(String name, String value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Variant name must not be null or blank");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Variant value must not be null or blank");
        }
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductVariant)) {
            return false;
        }
        ProductVariant variant = (ProductVariant) o;
        return name.equals(variant.name) && value.equals(variant.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
