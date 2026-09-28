package Application.adapters.out.persistence.mysql.adapters;

import Application.adapters.out.persistence.mysql.mappers.PersonMapper;
import Application.adapters.out.persistence.mysql.repositories.PersonJpaRepository;
import Application.domain.models.Person;
import Application.domain.ports.out.PersonRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MySQL persistence adapter for the PersonRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlPersonRepositoryAdapter implements PersonRepository {

    private final PersonJpaRepository jpaRepository;

    public MysqlPersonRepositoryAdapter(PersonJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(Person person) {
        jpaRepository.findById(person.getIdentifier()).ifPresentOrElse(
                existing -> {
                    PersonMapper.updateEntity(existing, person);
                    jpaRepository.save(existing);
                },
                () -> jpaRepository.save(PersonMapper.toEntity(person)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Person> findById(String identifier) {
        return jpaRepository.findById(identifier)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Person> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdentifier(String identifier) {
        return jpaRepository.existsById(identifier);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private Person requirePerson(String identifier) {
        return jpaRepository.findById(identifier)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson))
                .orElseThrow(() -> new IllegalStateException("Referenced person '"
                        + identifier + "' does not exist"));
    }
}
