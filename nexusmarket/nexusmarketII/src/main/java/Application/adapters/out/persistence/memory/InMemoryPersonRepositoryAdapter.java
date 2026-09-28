package Application.adapters.out.persistence.memory;

import Application.domain.models.Person;
import Application.domain.ports.out.PersonRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * In-memory adapter for the PersonRepository output port (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryPersonRepositoryAdapter implements PersonRepository {

    private final InMemoryDataStore store;

    public InMemoryPersonRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void save(Person person) {
        store.persons.put(person.getIdentifier(), person);
    }

    @Override
    public Optional<Person> findById(String identifier) {
        return Optional.ofNullable(store.persons.get(identifier));
    }

    @Override
    public Optional<Person> findByEmail(String email) {
        return store.persons.values().stream()
                .filter(person -> person.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return store.persons.containsKey(identifier);
    }

    @Override
    public boolean existsByEmail(String email) {
        return store.persons.values().stream()
                .anyMatch(person -> person.getEmail().equalsIgnoreCase(email));
    }
}
