package Application.domain.ports.in;

import Application.domain.models.Product;

/**
 * Input port (use case): publishes a Product in the catalog on behalf
 * of a Seller.
 */
public interface PublishProductUseCase {

    /**
     * @param sellerId  identifier of the Seller publishing the product
     * @param product   product to publish (built from the request by the adapter mapper)
     * @return the published Product
     */
    Product publishProduct(String sellerId, Product product);
}
