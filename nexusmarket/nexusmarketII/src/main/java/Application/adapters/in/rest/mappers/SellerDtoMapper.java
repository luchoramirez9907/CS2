package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.SellerResponse;
import Application.domain.models.Seller;

/**
 * Mapper: Domain Model (Seller) to Response DTO.
 */
public final class SellerDtoMapper {

    private SellerDtoMapper() {
    }

    public static SellerResponse toResponse(Seller seller) {
        return new SellerResponse(
                seller.getIdentifier(),
                seller.getFullName(),
                seller.getEmail(),
                seller.getRole().getCode(),
                seller.getStatus().getCode(),
                seller.getRegisteredBy().getIdentifier());
    }
}
