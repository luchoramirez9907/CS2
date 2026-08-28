package Application.domain.models;

import Application.domain.valueobjects.ProductStatus;

/**
 * PhysicalProduct
 *
 * Represents a tangible good that requires inventory management and
 * physical dispatch through a Shipment.
 */
public class PhysicalProduct extends Product {

    public PhysicalProduct(String identifier, String name, String description,
                           ProductStatus status, Seller seller) {
        super(identifier, name, description, status, seller);
    }

    @Override
    public boolean requiresPhysicalDispatch() {
        return true;
    }
}
