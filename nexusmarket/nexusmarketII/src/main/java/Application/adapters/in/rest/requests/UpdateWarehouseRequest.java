package Application.adapters.in.rest.requests;

/**
 * Request DTO: PUT /api/warehouses/{id}
 */
public record UpdateWarehouseRequest(String name, AddressRequest address) {
}
