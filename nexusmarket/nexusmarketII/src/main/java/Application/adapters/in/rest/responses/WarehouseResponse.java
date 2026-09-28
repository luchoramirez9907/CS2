package Application.adapters.in.rest.responses;

/**
 * Response DTO: warehouse information.
 */
public record WarehouseResponse(String identifier, String name, String ownership,
                                String street, String city, String state,
                                String country, String postalCode,
                                String managedById, String ownerSellerId) {
}
