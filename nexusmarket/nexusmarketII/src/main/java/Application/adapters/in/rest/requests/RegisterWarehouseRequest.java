package Application.adapters.in.rest.requests;

/**
 * Request DTO: register a warehouse. The ownership field selects whether
 * the warehouse belongs to the Marketplace or to a Seller.
 */
public record RegisterWarehouseRequest(String performerId, String warehouseId, String name,
                                       String street, String city, String state,
                                       String country, String postalCode,
                                       String ownership) {
}
