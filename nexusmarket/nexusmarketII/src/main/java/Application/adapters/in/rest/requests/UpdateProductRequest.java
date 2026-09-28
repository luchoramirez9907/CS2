package Application.adapters.in.rest.requests;

import java.util.List;

/**
 * Request DTO: PUT /api/products/{id}
 * variants: null keeps the current variants.
 */
public record UpdateProductRequest(String name, String description,
                                   List<PublishProductRequest.VariantRequest> variants) {
}
