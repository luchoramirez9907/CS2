package application.adapters.out.persistence.memory;

import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * In-memory adapter for the UserRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryUserRepositoryAdapter implements UserRepository {

    private final InMemoryDataStore store;

    public InMemoryUserRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(User user) {
        store.users.put(user.getIdentifier(), user);
    }

    @Override
    public Optional<User> findByPersonId(String personIdentifier) {
        return Optional.ofNullable(store.users.get(personIdentifier));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return store.users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
