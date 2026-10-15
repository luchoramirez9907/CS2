package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST /api/v1/admin/sellers
 * The registering administrator is the authenticated user.
 */
public record RegisterSellerRequest(@NotBlank @Size(max = 64) @Pattern(regexp = "\\S+") String identifier,
                                    @NotBlank @Size(max = 160) String fullName,
                                    @NotBlank @Email @Size(max = 160) String email,
                                    @NotBlank @Size(min = 8, max = 100) String password) {

    /** Never exposes the password (e.g. in logs). */
    @Override
    public String toString() {
        return "RegisterSellerRequest{identifier='" + identifier + "', email='" + email + "', password=***}";
    }
}
