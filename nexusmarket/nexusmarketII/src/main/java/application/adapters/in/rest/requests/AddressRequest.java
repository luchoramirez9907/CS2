package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: physical address (buyer delivery or warehouse location).
 */
public record AddressRequest(@NotBlank @Size(max = 200) String street,
                             @NotBlank @Size(max = 100) String city,
                             @NotBlank @Size(max = 100) String state,
                             @NotBlank @Size(max = 100) String country,
                             @NotBlank @Size(max = 20) String postalCode) {
}
