package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO: PUT buyer profile. primaryAddress and additionalAddresses:
 * null keeps the current value.
 */
public record UpdateBuyerRequest(@NotBlank @Size(max = 160) String fullName,
                                 @NotBlank @Email @Size(max = 160) String email,
                                 @Valid AddressRequest primaryAddress,
                                 List<@Valid AddressRequest> additionalAddresses) {
}
