package Application.adapters.in.rest.requests;

/**
 * Request DTO: register a new buyer (self-registration).
 */
public record RegisterBuyerRequest(String identifier, String fullName, String email,
                                   String street, String city, String state,
                                   String country, String postalCode) {
}
