package Application.adapters.in.rest.requests;

/**
 * Request DTO: update the description of a published product.
 */
public record UpdateProductRequest(String performerId, String newDescription) {
}
