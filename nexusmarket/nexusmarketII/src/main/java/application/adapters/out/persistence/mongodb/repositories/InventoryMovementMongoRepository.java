package application.adapters.out.persistence.mongodb.repositories;

import application.adapters.out.persistence.mongodb.documents.InventoryMovementDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoDB repository for inventory movement documents.
 */
public interface InventoryMovementMongoRepository extends MongoRepository<InventoryMovementDocument, String> {

    List<InventoryMovementDocument> findByInventoryIdOrderByMovementNumberAsc(String inventoryId);
}
