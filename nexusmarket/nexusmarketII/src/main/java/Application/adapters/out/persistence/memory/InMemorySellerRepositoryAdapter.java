package Application.adapters.out.persistence.memory;

import Application.domain.models.Seller;
import Application.domain.ports.out.SellerRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * In-memory adapter for the SellerRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemorySellerRepositoryAdapter implements SellerRepository {

    private final InMemoryDataStore store;

    public InMemorySellerRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(Seller seller) {
        store.persons.put(seller.getIdentifier(), seller);
    }

    @Override
    public Optional<Seller> findById(String identifier) {
        return Optional.ofNullable(store.persons.get(identifier))
                .filter(Seller.class::isInstance)
                .map(Seller.class::cast);
    }
}
