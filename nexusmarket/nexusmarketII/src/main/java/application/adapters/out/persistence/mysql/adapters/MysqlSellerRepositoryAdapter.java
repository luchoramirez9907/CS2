package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.PersonMapper;
import application.adapters.out.persistence.mysql.repositories.PersonJpaRepository;
import application.domain.models.Person;
import application.domain.models.Seller;
import application.domain.ports.out.SellerRepository;
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
