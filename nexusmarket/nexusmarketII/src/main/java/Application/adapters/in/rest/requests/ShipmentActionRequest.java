package Application.adapters.in.rest.requests;

/**
 * Request DTO: update the status of a shipment (dispatch or deliver).
 */
public record ShipmentActionRequest(String operatorId) {
}
