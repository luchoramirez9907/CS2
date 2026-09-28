package Application.adapters.in.rest.responses;

/**
 * Response DTO: structured address.
 */
public record AddressResponse(String street, String city, String state, String country,
                              String postalCode) {
}
