package Application.domain.ports.in;

import Application.domain.models.Product;
import Application.domain.models.ProductVariant;

import java.util.List;

/**
 * Input port (use case): consults and updates an existing product.
 */
public interface ManageProductUseCase {

    /**
     * Published products are public. Suspended or discontinued products
     * are only visible to their seller, Administrators and Supervisors.
     *
     * @param requesterId identifier of the requesting user (null for anonymous catalog access)
     */
    Product consultProduct(String requesterId, String productId);

    /**
     * @param variants replaces the product variants (null keeps them)
     */
    Product updateProduct(String requesterId, String productId, String name, String description,
                          List<ProductVariant> variants);
}
