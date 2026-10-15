package application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO: published product information.
 */
public record ProductResponse(String identifier, String name, String description, BigDecimal price,
                              boolean digital, String status, String sellerId,
                              List<VariantResponse> variants) {

    public record VariantResponse(String name, String value) {
    }
}
