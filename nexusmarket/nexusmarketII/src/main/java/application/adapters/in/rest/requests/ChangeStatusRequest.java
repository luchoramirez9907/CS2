package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO: PATCH .../status
 * status: exact catalog code of the target status (e.g. ACTIVE, BLOCKED, SUSPENDED).
 */
public record ChangeStatusRequest(@NotBlank @Pattern(regexp = "ACTIVE|INACTIVE|BLOCKED|RESTRICTED"
                                          + "|PUBLISHED|SUSPENDED|DISCONTINUED") String status) {
}
