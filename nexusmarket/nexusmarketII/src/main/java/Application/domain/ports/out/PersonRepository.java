package Application.domain.ports.out;

import Application.domain.models.Person;

import java.util.Optional;

/**
 * Output port: persistence contract for any Person of the platform.
 * identifier and email must be unique across the platform (per SDD).
 */
public interface PersonRepository {

    void save(Person person);

    Optional<Person> findById(String identifier);

    Optional<Person> findByEmail(String email);

    boolean existsByIdentifier(String identifier);

    boolean existsByEmail(String email);
}
