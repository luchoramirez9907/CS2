package Application.domain.ports.out;

import java.time.LocalDateTime;

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
     * @param event      event name (e.g. CREATED, DISPATCHED, DELIVERED)
     * @param occurredAt time of the event
     * @param details    human-readable details of the event
     */
    void record(String shipmentId, String orderId, String event, LocalDateTime occurredAt, String details);
}
