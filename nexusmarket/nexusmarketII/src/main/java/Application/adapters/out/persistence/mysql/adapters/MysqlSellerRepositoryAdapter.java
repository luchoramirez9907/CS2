package Application.adapters.out.persistence.mysql.adapters;

import Application.adapters.out.persistence.mysql.mappers.PersonMapper;
import Application.adapters.out.persistence.mysql.repositories.PersonJpaRepository;
import Application.domain.models.Person;
import Application.domain.models.Seller;
import Application.domain.ports.out.SellerRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MySQL persistence adapter for the SellerRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlSellerRepositoryAdapter implements SellerRepository {

    private final PersonJpaRepository jpaRepository;

    public MysqlSellerRepositoryAdapter(PersonJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(Seller seller) {
        jpaRepository.findById(seller.getIdentifier()).ifPresentOrElse(
                existing -> {
                    PersonMapper.updateEntity(existing, seller);
                    jpaRepository.save(existing);
                },
                () -> jpaRepository.save(PersonMapper.toEntity(seller)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seller> findById(String identifier) {
        return jpaRepository.findById(identifier)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson))
                .filter(Seller.class::isInstance)
                .map(Seller.class::cast);
    }

    private Person requirePerson(String referencedId) {
        return jpaRepository.findById(referencedId)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson))
                .orElseThrow(() -> new IllegalStateException("Referenced person '"
                        + referencedId + "' does not exist"));
    }
}
