package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/buyers (header X-User-Id optional: administrator;
 * omitted for buyer self-registration)
 */
public record RegisterBuyerRequest(String identifier, String fullName, String email,
                                   AddressRequest primaryAddress) {
}
