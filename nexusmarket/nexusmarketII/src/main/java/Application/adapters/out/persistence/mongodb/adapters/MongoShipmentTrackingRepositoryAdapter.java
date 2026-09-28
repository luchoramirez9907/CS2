package Application.adapters.out.persistence.mongodb.adapters;

import Application.adapters.out.persistence.mongodb.documents.ShipmentTrackingDocument;
import Application.adapters.out.persistence.mongodb.repositories.ShipmentTrackingMongoRepository;
import Application.domain.models.ShipmentTrackingEvent;
import Application.domain.ports.out.ShipmentTrackingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * MongoDB adapter implementing the ShipmentTrackingRepository output
 * port (shipment tracking event store).
 */
@Repository
@Profile("!memory")
public class MongoShipmentTrackingRepositoryAdapter implements ShipmentTrackingRepository {

    private final ShipmentTrackingMongoRepository mongoRepository;

    public MongoShipmentTrackingRepositoryAdapter(ShipmentTrackingMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public void record(String shipmentId, String orderId, String event,
                       LocalDateTime occurredAt, String details) {
        mongoRepository.save(new ShipmentTrackingDocument(
                UUID.randomUUID().toString(), shipmentId, orderId, event, occurredAt, details));
    }

    @Override
    public List<ShipmentTrackingEvent> findByShipmentId(String shipmentId) {
        return mongoRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId).stream()
                .map(document -> new ShipmentTrackingEvent(document.getShipmentId(), document.getOrderId(),
                        document.getEvent(), document.getOccurredAt(), document.getDetails()))
                .toList();
    }
}
