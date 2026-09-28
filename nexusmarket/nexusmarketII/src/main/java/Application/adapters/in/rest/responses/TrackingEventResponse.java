package Application.adapters.in.rest.responses;

/**
 * Response DTO: shipment tracking event.
 */
public record TrackingEventResponse(String event, String occurredAt, String details) {
}
