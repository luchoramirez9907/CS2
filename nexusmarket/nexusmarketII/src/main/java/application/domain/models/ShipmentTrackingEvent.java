package application.domain.models;

import application.domain.enums.TrackingEventType;

import java.time.LocalDateTime;

/**
 * ShipmentTrackingEvent
 *
 * Represents a single step recorded in the tracking history of a shipment
 * (e.g. CREATED, DISPATCHED, DELIVERED).
 */
public record ShipmentTrackingEvent(String shipmentId, String orderId, TrackingEventType event,
                                    LocalDateTime occurredAt, String details) {
}
