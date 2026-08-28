package Application.domain.models;

import Application.domain.valueobjects.ProductStatus;

/**
 * DigitalProduct
 *
 * Represents an intangible good delivered immediately upon payment
 * confirmation, without requiring inventory or physical dispatch.
 */
public class DigitalProduct extends Product {

    private final String digitalDeliveryDetails;

    public DigitalProduct(String identifier, String name, String description,
                          ProductStatus status, Seller seller, String digitalDeliveryDetails) {
        super(identifier, name, description, status, seller);
        if (digitalDeliveryDetails == null || digitalDeliveryDetails.isBlank()) {
            throw new IllegalArgumentException(
                    "A DigitalProduct requires digital delivery details to deliver the good to the buyer");
        }
        this.digitalDeliveryDetails = digitalDeliveryDetails;
    }

    public String getDigitalDeliveryDetails() {
        return digitalDeliveryDetails;
    }

    @Override
    public boolean requiresPhysicalDispatch() {
        return false;
    }
}
