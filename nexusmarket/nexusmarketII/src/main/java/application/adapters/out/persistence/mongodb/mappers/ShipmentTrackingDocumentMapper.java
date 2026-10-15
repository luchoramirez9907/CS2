package application.adapters.out.persistence.mongodb.mappers;

import application.adapters.out.persistence.mongodb.documents.ShipmentTrackingDocument;
import application.domain.enums.TrackingEventType;
import application.domain.models.ShipmentTrackingEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper: ShipmentTrackingEvent domain records to MongoDB documents and back.
 */
public final class ShipmentTrackingDocumentMapper {

    private ShipmentTrackingDocumentMapper() {
    }

    public static ShipmentTrackingDocument toDocument(String shipmentId, String orderId, TrackingEventType event,
                                                      LocalDateTime occurredAt, String details) {
        return new ShipmentTrackingDocument(UUID.randomUUID().toString(), shipmentId, orderId, event.name(),
                occurredAt, details);
    }

    public static ShipmentTrackingEvent toDomain(ShipmentTrackingDocument document) {
        return new ShipmentTrackingEvent(document.getShipmentId(), document.getOrderId(),
                TrackingEventType.valueOf(document.getEvent()), document.getOccurredAt(), document.getDetails());
    }
}
