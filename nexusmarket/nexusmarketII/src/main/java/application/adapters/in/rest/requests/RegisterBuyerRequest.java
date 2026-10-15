package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST /api/v1/public/buyers (self-registration) and
 * POST /api/v1/admin/buyers (registration by an administrator).
 */
public record RegisterBuyerRequest(@NotBlank @Size(max = 64) @Pattern(regexp = "\\S+") String identifier,
                                   @NotBlank @Size(max = 160) String fullName,
                                   @NotBlank @Email @Size(max = 160) String email,
                                   @NotNull @Valid AddressRequest primaryAddress,
                                   @NotBlank @Size(min = 8, max = 100) String password) {

    /** Never exposes the password (e.g. in logs). */
    @Override
    public String toString() {
        return "RegisterBuyerRequest{identifier='" + identifier + "', email='" + email + "', password=***}";
    }
}
