package Application.adapters.in.rest.responses;

import java.util.List;

/**
 * Response DTO: buyer information.
 */
public record BuyerResponse(String identifier, String fullName, String email, String status,
                            String commercialStatus, AddressResponse primaryAddress,
                            List<AddressResponse> additionalAddresses) {
}
