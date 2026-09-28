package Application.adapters.out.persistence.mongodb.repositories;

import Application.adapters.out.persistence.mongodb.documents.ShipmentTrackingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoDB repository for shipment tracking documents.
 */
public interface ShipmentTrackingMongoRepository extends MongoRepository<ShipmentTrackingDocument, String> {

    List<ShipmentTrackingDocument> findByShipmentIdOrderByOccurredAtAsc(String shipmentId);
}
