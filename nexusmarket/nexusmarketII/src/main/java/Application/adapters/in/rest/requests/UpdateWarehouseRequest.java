package Application.adapters.in.rest.requests;

/**
 * Request DTO: update warehouse information (name).
 */
public record UpdateWarehouseRequest(String performerId, String newName) {
}
