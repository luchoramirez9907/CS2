package Application.adapters.in.rest.requests;

/**
 * Request DTO: POST /api/returns/{returnId}/approval
 */
public record ApproveReturnRequest(String administratorId) {
}
