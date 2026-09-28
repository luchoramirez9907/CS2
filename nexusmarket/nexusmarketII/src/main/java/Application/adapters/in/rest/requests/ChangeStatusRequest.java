package Application.adapters.in.rest.requests;

/**
 * Request DTO: PATCH .../status (status code of the corresponding catalog,
 * e.g. ACTIVE, INACTIVE, BLOCKED, RESTRICTED, SUSPENDED, DISCONTINUED).
 */
public record ChangeStatusRequest(String status) {
}
