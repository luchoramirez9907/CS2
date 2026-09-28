package Application.adapters.in.rest.requests;

/**
 * Request DTO: change the lifecycle status of a product.
 */
public record ChangeProductStatusRequest(String performerId, String statusCode) {
}
