package Application.adapters.out.persistence.mysql.adapters;

import Application.adapters.out.persistence.mysql.mappers.PersonMapper;
import Application.adapters.out.persistence.mysql.mappers.WarehouseMapper;
import Application.adapters.out.persistence.mysql.repositories.PersonJpaRepository;
import Application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import Application.domain.models.Person;
import Application.domain.models.Warehouse;
import Application.domain.ports.out.WarehouseRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MySQL persistence adapter for the WarehouseRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlWarehouseRepositoryAdapter implements WarehouseRepository {

    private final WarehouseJpaRepository jpaRepository;
    private final PersonJpaRepository personJpaRepository;

    public MysqlWarehouseRepositoryAdapter(WarehouseJpaRepository jpaRepository,
                                           PersonJpaRepository personJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.personJpaRepository = personJpaRepository;
    }

    @Override
    @Transactional
    public void save(Warehouse warehouse) {
        jpaRepository.findById(warehouse.getIdentifier()).ifPresentOrElse(
                existing -> {
                    existing.setName(warehouse.getName());
                    existing.setAddress(PersonMapper.toEmbeddable(warehouse.getAddress()));
                    jpaRepository.save(existing);
                },
                () -> jpaRepository.save(WarehouseMapper.toEntity(warehouse)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Warehouse> findById(String identifier) {
        return jpaRepository.findById(identifier)
                .map(entity -> WarehouseMapper.toDomain(entity, this::requirePerson));
    }

    private Person requirePerson(String referencedId) {
        return personJpaRepository.findById(referencedId)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson))
                .orElseThrow(() -> new IllegalStateException("Referenced person '"
                        + referencedId + "' does not exist"));
    }
}
