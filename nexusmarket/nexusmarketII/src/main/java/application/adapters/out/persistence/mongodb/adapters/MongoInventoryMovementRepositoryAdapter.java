package application.adapters.out.persistence.mongodb.adapters;

import application.adapters.out.persistence.mongodb.mappers.InventoryMovementDocumentMapper;
import application.adapters.out.persistence.mongodb.repositories.InventoryMovementMongoRepository;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Person;
import application.domain.ports.out.InventoryMovementRepository;
import application.domain.ports.out.PersonRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB adapter implementing the InventoryMovementRepository output
 * port (movement history store). The person who performed each movement
 * is resolved through the PersonRepository output port.
 */
@Repository
@Profile("!memory")
public class MongoInventoryMovementRepositoryAdapter implements InventoryMovementRepository {

    private final InventoryMovementMongoRepository mongoRepository;
    private final PersonRepository personRepository;

    public MongoInventoryMovementRepositoryAdapter(InventoryMovementMongoRepository mongoRepository,
                                                   PersonRepository personRepository) {
        this.mongoRepository = mongoRepository;
        this.personRepository = personRepository;
    }

    @Override
    public void save(InventoryMovement movement) {
        mongoRepository.save(InventoryMovementDocumentMapper.toDocument(movement));
    }

    @Override
    public List<InventoryMovement> findByInventory(Inventory inventory) {
        return mongoRepository.findByInventoryIdOrderByMovementNumberAsc(inventory.getIdentifier()).stream()
                .map(document -> InventoryMovementDocumentMapper.toDomain(document, inventory,
                        requirePerson(document.getPerformedById())))
                .toList();
    }

    private Person requirePerson(String identifier) {
        return personRepository.findById(identifier)
                .orElseThrow(() -> new IllegalStateException("Person '" + identifier
                        + "' referenced by an inventory movement does not exist"));
    }
}
