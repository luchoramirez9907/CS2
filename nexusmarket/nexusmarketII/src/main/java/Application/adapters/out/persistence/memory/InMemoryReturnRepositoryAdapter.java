package Application.adapters.out.persistence.memory;

import Application.domain.models.Return;
import Application.domain.ports.out.ReturnRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * In-memory adapter for the ReturnRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryReturnRepositoryAdapter implements ReturnRepository {

    private final InMemoryDataStore store;

    public InMemoryReturnRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(Return returnRequest) {
        store.returns.put(returnRequest.getReturnId(), returnRequest);
    }

    @Override
    public Optional<Return> findById(String returnId) {
        return Optional.ofNullable(store.returns.get(returnId));
    }
}
