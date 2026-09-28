package Application.domain.ports.out;

import Application.domain.models.Product;

import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence contract for Products of the catalog.
 */
public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(String identifier);

    /** Every product published by the given seller (Seller performance). */
    List<Product> findBySellerId(String sellerId);
}
