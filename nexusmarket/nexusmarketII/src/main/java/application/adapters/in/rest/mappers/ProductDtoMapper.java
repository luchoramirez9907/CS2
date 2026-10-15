package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.PublishProductRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.domain.models.DigitalProduct;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.valueobjects.ProductStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper: Request DTO to Domain Model and Domain Model to Response DTO
 * for products.
 */
public final class ProductDtoMapper {

    private ProductDtoMapper() {
    }

    /**
     * Builds the product domain model from the request. The seller is
     * bound later by the domain service (ownership validation happens in
     * the domain).
     */
    public static Product toDomain(PublishProductRequest request, application.domain.models.Seller seller) {
        Product product = request.digital()
                ? new DigitalProduct(request.identifier(), request.name(), request.description(), request.price(),
                        ProductStatus.PUBLISHED, seller, request.digitalDeliveryDetails())
                : new PhysicalProduct(request.identifier(), request.name(), request.description(), request.price(),
                        ProductStatus.PUBLISHED, seller);
        if (request.variants() != null) {
            for (PublishProductRequest.VariantRequest variant : request.variants()) {
                product.addVariant(variant.name(), variant.value());
            }
        }
        return product;
    }

    public static List<ProductVariant> toVariants(List<PublishProductRequest.VariantRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(variant -> new ProductVariant(variant.name(), variant.value()))
                .toList();
    }

    public static ProductResponse toResponse(Product product) {
        List<ProductResponse.VariantResponse> variants = new ArrayList<>();
        for (ProductVariant variant : product.getVariants()) {
            variants.add(new ProductResponse.VariantResponse(variant.getName(), variant.getValue()));
        }
        return new ProductResponse(
                product.getIdentifier(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                !product.requiresPhysicalDispatch(),
                product.getStatus().getCode(),
                product.getSeller().getIdentifier(),
                variants);
    }
}
