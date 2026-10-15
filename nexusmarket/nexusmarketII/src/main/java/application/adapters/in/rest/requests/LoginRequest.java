package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: POST /api/v1/auth/login
 */
public record LoginRequest(@NotBlank @Size(max = 160) String email,
                           @NotBlank @Size(max = 100) String password) {

    /** Never exposes the password (e.g. in logs). */
    @Override
    public String toString() {
        return "LoginRequest{email='" + email + "', password=***}";
    }
}
