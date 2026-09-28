package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.BuyerResponse;
import Application.domain.models.Buyer;

/**
 * Mapper: Domain Model (Buyer) to Response DTO.
 */
public final class BuyerDtoMapper {

    private BuyerDtoMapper() {
    }

    public static BuyerResponse toResponse(Buyer buyer) {
        return new BuyerResponse(
                buyer.getIdentifier(),
                buyer.getFullName(),
                buyer.getEmail(),
                buyer.getRole().getCode(),
                buyer.getStatus().getCode(),
                buyer.getCommercialStatus().getCode());
    }
}
