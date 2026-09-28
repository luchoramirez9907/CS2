package Application.domain.ports.in;

import Application.domain.models.Product;

/**
 * Input port (use case): Manage Product.
 *
 * Consults and updates the information of an existing product according
 * to the applicable business rules.
 */
public interface ManageProductUseCase {

    Product consultProduct(String requesterId, String productId);

    /**
     * @param performerId    identifier of the owning Seller
     * @param productId      identifier of the product to update
     * @param newDescription new description of the product
     * @return the updated Product
     */
    Product updateProductDescription(String performerId, String productId, String newDescription);
}
