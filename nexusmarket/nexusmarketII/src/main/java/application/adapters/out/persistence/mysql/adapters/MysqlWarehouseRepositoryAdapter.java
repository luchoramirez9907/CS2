package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.PersonMapper;
import application.adapters.out.persistence.mysql.mappers.WarehouseMapper;
import application.adapters.out.persistence.mysql.repositories.PersonJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.models.Person;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> findAll() {
        return jpaRepository.findAll().stream()
                .map(entity -> WarehouseMapper.toDomain(entity, this::requirePerson))
                .toList();
    }

    private Person requirePerson(String referencedId) {
        return personJpaRepository.findById(referencedId)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson))
                .orElseThrow(() -> new IllegalStateException("Referenced person '"
                        + referencedId + "' does not exist"));
    }
}
