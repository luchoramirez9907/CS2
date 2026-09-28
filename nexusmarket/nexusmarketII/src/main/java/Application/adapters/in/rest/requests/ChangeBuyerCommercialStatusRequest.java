package Application.adapters.in.rest.requests;

/**
 * Request DTO: change the commercial status of a buyer.
 */
public record ChangeBuyerCommercialStatusRequest(String performerId, String buyerId,
                                                 String statusCode) {
}
