package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/warehouses
 * ownerSellerId: null for a Marketplace warehouse.
 */
public record RegisterWarehouseRequest(String identifier, String name, AddressRequest address,
                                       String ownerSellerId) {
}
