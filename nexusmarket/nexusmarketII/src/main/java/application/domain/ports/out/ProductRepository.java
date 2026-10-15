package application.domain.ports.out;

import application.domain.models.Product;

import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence contract for Products of the catalog.
 */
public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(String identifier);

    List<Product> findBySellerId(String sellerId);

    List<Product> findAll();
}
