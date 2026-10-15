package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO: update of the identity information of a participant.
 */
public record UpdatePersonRequest(@NotBlank @Size(max = 160) String fullName,
                                  @NotBlank @Email @Size(max = 160) String email) {
}
