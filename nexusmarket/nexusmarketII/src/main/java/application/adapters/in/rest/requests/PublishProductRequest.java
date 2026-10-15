package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO: POST /api/v1/seller/products
 * digital=false -> PhysicalProduct; digital=true -> DigitalProduct (requires
 * digitalDeliveryDetails). The seller is the authenticated user.
 */
public record PublishProductRequest(@NotBlank @Size(max = 64) String identifier,
                                    @NotBlank @Size(max = 160) String name,
                                    @NotNull @PositiveOrZero @Digits(integer = 13, fraction = 2) BigDecimal price,
                                    @Size(max = 1000) String description,
                                    boolean digital,
                                    @Size(max = 500) String digitalDeliveryDetails,
                                    List<@Valid VariantRequest> variants) {

    public record VariantRequest(@NotBlank @Size(max = 60) String name, @NotBlank @Size(max = 120) String value) {
    }
}
