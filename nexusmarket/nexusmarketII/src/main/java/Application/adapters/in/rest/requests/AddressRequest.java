package Application.adapters.in.rest.requests;

/**
 * Request DTO: structured address (Address value object).
 */
public record AddressRequest(String street, String city, String state, String country,
                             String postalCode) {
}
