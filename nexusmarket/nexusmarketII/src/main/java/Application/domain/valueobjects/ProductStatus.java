package Application.domain.valueobjects;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ProductStatus
 *
 * Represents the lifecycle state of a product within the catalog.
 */
public final class ProductStatus extends DomainCatalog {

    public static final ProductStatus PUBLISHED =
            new ProductStatus("PUBLISHED", "Published",
                    "Product is visible and available in the public catalog.");
    public static final ProductStatus SUSPENDED =
            new ProductStatus("SUSPENDED", "Suspended",
                    "Product is temporarily hidden from the public catalog.");
    public static final ProductStatus DISCONTINUED =
            new ProductStatus("DISCONTINUED", "Discontinued",
                    "Product is permanently removed from commercialization.");

    private static final Map<String, ProductStatus> VALUES_BY_CODE;

    static {
        Map<String, ProductStatus> values = new LinkedHashMap<>();
        values.put(PUBLISHED.code(), PUBLISHED);
        values.put(SUSPENDED.code(), SUSPENDED);
        values.put(DISCONTINUED.code(), DISCONTINUED);
        VALUES_BY_CODE = Collections.unmodifiableMap(values);
    }

    private ProductStatus(String code, String name, String description) {
        super(code, name, description);
    }

    public static ProductStatus fromCode(String code) {
        ProductStatus status = VALUES_BY_CODE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Unknown ProductStatus code: " + code);
        }
        return status;
    }

    public static Collection<ProductStatus> values() {
        return VALUES_BY_CODE.values();
    }

    private String code() {
        return getCode();
    }
}
