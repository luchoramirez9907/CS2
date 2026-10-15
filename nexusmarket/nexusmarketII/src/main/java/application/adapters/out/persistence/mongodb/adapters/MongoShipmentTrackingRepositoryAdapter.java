package application.adapters.out.persistence.mongodb.adapters;

import application.adapters.out.persistence.mongodb.mappers.ShipmentTrackingDocumentMapper;
import application.adapters.out.persistence.mongodb.repositories.ShipmentTrackingMongoRepository;
import application.domain.enums.TrackingEventType;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.ports.out.ShipmentTrackingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
    public void record(String shipmentId, String orderId, TrackingEventType event,
                       LocalDateTime occurredAt, String details) {
        mongoRepository.save(ShipmentTrackingDocumentMapper.toDocument(shipmentId, orderId, event,
                occurredAt, details));
    }

    @Override
    public List<ShipmentTrackingEvent> findByShipmentId(String shipmentId) {
        return mongoRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId).stream()
                .map(ShipmentTrackingDocumentMapper::toDomain)
                .toList();
    }
}
