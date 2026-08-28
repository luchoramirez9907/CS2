package Application.adapters.in.rest.responses;

/**
 * Response DTO: return request information.
 */
public record ReturnResponse(String returnId, String orderId, String buyerId,
                             String reason, String returnStatus, String requestDate) {
}
