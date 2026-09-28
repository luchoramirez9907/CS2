package Application.adapters.in.rest.requests;

import java.util.List;

/**
 * Request DTO: PUT /api/buyers/{id}
 * primaryAddress / additionalAddresses: null keeps the current value.
 */
public record UpdateBuyerRequest(String fullName, String email, AddressRequest primaryAddress,
                                 List<AddressRequest> additionalAddresses) {
}
