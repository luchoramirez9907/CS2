package Application.adapters.out.persistence.mongodb.adapters;

import Application.adapters.out.persistence.mongodb.documents.InventoryMovementDocument;
import Application.adapters.out.persistence.mongodb.repositories.InventoryMovementMongoRepository;
import Application.domain.models.InventoryMovement;
import Application.domain.ports.out.InventoryMovementRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * MongoDB adapter implementing the InventoryMovementRepository output
 * port (movement history store).
 */
@Repository
@Profile("!memory")
public class MongoInventoryMovementRepositoryAdapter implements InventoryMovementRepository {

    private final InventoryMovementMongoRepository mongoRepository;

    public MongoInventoryMovementRepositoryAdapter(InventoryMovementMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public void save(InventoryMovement movement) {
        mongoRepository.save(new InventoryMovementDocument(
                UUID.randomUUID().toString(),
                movement.getInventory().getIdentifier(),
                movement.getMovementId(),
                movement.getInventory().getProduct().getIdentifier(),
                movement.getMovementType().getCode(),
                movement.getQuantity(),
                movement.getMovementDate(),
                movement.getPerformedBy().getIdentifier()));
    }
}
