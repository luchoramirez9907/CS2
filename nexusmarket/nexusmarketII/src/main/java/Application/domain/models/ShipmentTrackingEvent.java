package Application.domain.models;

import java.time.LocalDateTime;

/**
 * ShipmentTrackingEvent
 *
 * Represents a single step recorded in the tracking history of a shipment
 * (e.g. CREATED, DISPATCHED, DELIVERED).
 */
public record ShipmentTrackingEvent(String shipmentId, String orderId, String event,
                                    LocalDateTime occurredAt, String details) {
}
