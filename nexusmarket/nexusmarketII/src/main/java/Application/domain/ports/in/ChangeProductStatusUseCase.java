package Application.domain.ports.in;

import Application.domain.models.Product;
import Application.domain.valueobjects.ProductStatus;

/**
 * Input port (use case): suspends, re-publishes or permanently
 * discontinues a product, hiding or removing it from the public catalog.
 */
public interface ChangeProductStatusUseCase {

    Product changeProductStatus(String requesterId, String productId, ProductStatus status);
}
