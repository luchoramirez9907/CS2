package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.UserMapper;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.domain.models.User;
import application.domain.ports.out.PersonRepository;
import application.domain.ports.out.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MySQL adapter implementing the UserRepository output port. The owning
 * Person is resolved through the PersonRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlUserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final PersonRepository personRepository;

    public MysqlUserRepositoryAdapter(UserJpaRepository jpaRepository, PersonRepository personRepository) {
        this.jpaRepository = jpaRepository;
        this.personRepository = personRepository;
    }

    @Override
    @Transactional
    public void save(User user) {
        jpaRepository.save(UserMapper.toEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByPersonId(String personIdentifier) {
        return jpaRepository.findById(personIdentifier)
                .flatMap(entity -> personRepository.findById(entity.getPersonId())
                        .map(person -> UserMapper.toDomain(entity, person)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return personRepository.findByEmail(email)
                .flatMap(person -> jpaRepository.findById(person.getIdentifier())
                        .map(entity -> UserMapper.toDomain(entity, person)));
    }
}
