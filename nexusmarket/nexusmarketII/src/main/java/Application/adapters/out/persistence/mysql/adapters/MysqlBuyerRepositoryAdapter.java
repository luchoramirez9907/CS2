package Application.adapters.out.persistence.mysql.adapters;

import Application.adapters.out.persistence.mysql.mappers.PersonMapper;
import Application.adapters.out.persistence.mysql.repositories.PersonJpaRepository;
import Application.domain.models.Buyer;
import Application.domain.models.Person;
import Application.domain.ports.out.BuyerRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MySQL persistence adapter for the BuyerRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlBuyerRepositoryAdapter implements BuyerRepository {

    private final PersonJpaRepository jpaRepository;

    public MysqlBuyerRepositoryAdapter(PersonJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(Buyer buyer) {
        jpaRepository.findById(buyer.getIdentifier()).ifPresentOrElse(
                existing -> {
                    PersonMapper.updateEntity(existing, buyer);
                    jpaRepository.save(existing);
                },
                () -> jpaRepository.save(PersonMapper.toEntity(buyer)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Buyer> findById(String identifier) {
        return jpaRepository.findById(identifier)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson))
                .filter(Buyer.class::isInstance)
                .map(Buyer.class::cast);
    }

    private Person requirePerson(String referencedId) {
        return jpaRepository.findById(referencedId)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson))
                .orElseThrow(() -> new IllegalStateException("Referenced person '"
                        + referencedId + "' does not exist"));
    }
}
