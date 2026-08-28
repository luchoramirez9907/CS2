package Application.adapters.out.persistence.mongodb.repositories;

import Application.adapters.out.persistence.mongodb.documents.InventoryMovementDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * MongoDB repository for inventory movement documents.
 */
public interface InventoryMovementMongoRepository extends MongoRepository<InventoryMovementDocument, String> {
}
