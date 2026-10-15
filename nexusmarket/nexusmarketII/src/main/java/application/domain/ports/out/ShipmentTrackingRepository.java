package application.domain.ports.out;

import application.domain.enums.TrackingEventType;
import application.domain.models.ShipmentTrackingEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Output port: persistence contract for shipment tracking events
 * (flexible, high-volume operational data, stored e.g. in MongoDB).
 */
public interface ShipmentTrackingRepository {

    /**
     * Records a tracking event for a shipment.
     *
     * @param shipmentId identifier of the shipment
     * @param orderId    identifier of the fulfilled order
     * @param event      type of tracking event
     * @param occurredAt time of the event
     * @param details    human-readable details of the event
     */
    void record(String shipmentId, String orderId, TrackingEventType event, LocalDateTime occurredAt, String details);

    /**
     * @return the tracking history of a shipment, oldest event first
     */
    List<ShipmentTrackingEvent> findByShipmentId(String shipmentId);
}
