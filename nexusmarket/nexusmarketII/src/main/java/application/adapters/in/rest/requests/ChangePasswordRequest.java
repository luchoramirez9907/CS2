package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: PUT /api/v1/auth/password
 */
public record ChangePasswordRequest(@NotBlank @Size(max = 100) String currentPassword,
                                    @NotBlank @Size(min = 8, max = 100) String newPassword) {

    /** Never exposes the passwords (e.g. in logs). */
    @Override
    public String toString() {
        return "ChangePasswordRequest{currentPassword=***, newPassword=***}";
    }
}
