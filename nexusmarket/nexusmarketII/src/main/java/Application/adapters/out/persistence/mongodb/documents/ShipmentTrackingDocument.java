package Application.adapters.out.persistence.mongodb.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for shipment tracking events (flexible, high-volume
 * operational data, per SDD - MongoDB Adapter).
 */
@Document(collection = "shipment_tracking")
public class ShipmentTrackingDocument {

    @Id
    private String id;

    private String shipmentId;

    private String orderId;

    private String event;

    private LocalDateTime occurredAt;

    private String details;

    public ShipmentTrackingDocument() {
    }

    public ShipmentTrackingDocument(String id, String shipmentId, String orderId, String event,
                                    LocalDateTime occurredAt, String details) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.event = event;
        this.occurredAt = occurredAt;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getEvent() {
        return event;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDetails() {
        return details;
    }
}
