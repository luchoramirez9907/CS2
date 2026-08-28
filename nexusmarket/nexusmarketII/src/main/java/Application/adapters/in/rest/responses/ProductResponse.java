package Application.adapters.in.rest.responses;

import java.util.List;

/**
 * Response DTO: published product information.
 */
public record ProductResponse(String identifier, String name, String description,
                              boolean digital, String status, String sellerId,
                              List<VariantResponse> variants) {

    public record VariantResponse(String name, String value) {
    }
}
