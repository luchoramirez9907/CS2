package Application.adapters.in.rest.responses;

/**
 * Response DTO: warehouse information.
 * ownership: MARKETPLACE (ownerId = managing administrator) or SELLER (ownerId = seller).
 */
public record WarehouseResponse(String identifier, String name, AddressResponse address,
                                String ownership, String ownerId) {
}
