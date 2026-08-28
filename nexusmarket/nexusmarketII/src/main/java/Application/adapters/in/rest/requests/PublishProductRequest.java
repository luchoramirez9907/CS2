package Application.adapters.in.rest.requests;

import java.util.List;

/**
 * Request DTO: POST /api/products
 * digital=false -> PhysicalProduct; digital=true -> DigitalProduct.
 */
public record PublishProductRequest(String sellerId, String identifier, String name,
                                    String description, boolean digital,
                                    String digitalDeliveryDetails,
                                    List<VariantRequest> variants) {

    public record VariantRequest(String name, String value) {
    }
}
