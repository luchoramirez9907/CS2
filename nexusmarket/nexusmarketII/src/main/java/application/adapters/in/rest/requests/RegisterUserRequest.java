package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST /api/v1/admin/users
 * role: ADMINISTRATOR, SUPERVISOR or LOGISTICS_OPERATOR.
 */
public record RegisterUserRequest(@NotBlank @Size(max = 64) @Pattern(regexp = "\\S+") String identifier,
                                  @NotBlank @Size(max = 160) String fullName,
                                  @NotBlank @Email @Size(max = 160) String email,
                                  @NotBlank @Pattern(regexp = "BUYER|SELLER|LOGISTICS_OPERATOR"
                                          + "|ADMINISTRATOR|SUPERVISOR") String role,
                                  @NotBlank @Size(min = 8, max = 100) String password) {

    /** Never exposes the password (e.g. in logs). */
    @Override
    public String toString() {
        return "RegisterUserRequest{identifier='" + identifier + "', email='" + email + "', password=***}";
    }
}
