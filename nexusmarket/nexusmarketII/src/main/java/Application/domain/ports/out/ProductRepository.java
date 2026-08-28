package Application.domain.ports.out;

import Application.domain.models.Product;

import java.util.Optional;

/**
 * Output port: persistence contract for Products of the catalog.
 */
public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(String identifier);
}
