package Application.adapters.out.persistence.memory;

import Application.domain.models.Buyer;
import Application.domain.ports.out.BuyerRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * In-memory adapter for the BuyerRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryBuyerRepositoryAdapter implements BuyerRepository {

    private final InMemoryDataStore store;

    public InMemoryBuyerRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(Buyer buyer) {
        store.persons.put(buyer.getIdentifier(), buyer);
    }

    @Override
    public Optional<Buyer> findById(String identifier) {
        return Optional.ofNullable(store.persons.get(identifier))
                .filter(Buyer.class::isInstance)
                .map(Buyer.class::cast);
    }
}
