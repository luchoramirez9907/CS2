package Application.domain.ports.in;

import Application.domain.models.Product;

/**
 * Input port (use case): Change Product Status.
 *
 * Suspends or permanently discontinues a product, removing or hiding it
 * from the public catalog.
 */
public interface ChangeProductStatusUseCase {

    /**
     * @param performerId identifier of the owning Seller
     * @param productId   identifier of the product
     * @param statusCode  one of PUBLISHED, SUSPENDED or DISCONTINUED
     * @return the updated Product
     */
    Product changeProductStatus(String performerId, String productId, String statusCode);
}
