package application.adapters.out.persistence.memory;

import application.domain.models.Product;
import application.domain.ports.out.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * In-memory adapter for the ProductRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryProductRepositoryAdapter implements ProductRepository {

    private final InMemoryDataStore store;

    public InMemoryProductRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(Product product) {
        store.products.put(product.getIdentifier(), product);
    }

    @Override
    public Optional<Product> findById(String identifier) {
        return Optional.ofNullable(store.products.get(identifier));
    }

    @Override
    public List<Product> findBySellerId(String sellerId) {
        return store.products.values().stream()
                .filter(product -> product.getSeller().getIdentifier().equals(sellerId))
                .toList();
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(store.products.values());
    }
}
