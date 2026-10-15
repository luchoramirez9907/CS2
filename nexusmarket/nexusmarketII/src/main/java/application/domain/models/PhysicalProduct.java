package application.domain.models;

import application.domain.valueobjects.ProductStatus;

import java.math.BigDecimal;

/**
 * PhysicalProduct
 *
 * Represents a tangible good that requires inventory management and
 * physical dispatch through a Shipment.
 */
public class PhysicalProduct extends Product {

    public PhysicalProduct(String identifier, String name, String description, BigDecimal price,
                           ProductStatus status, Seller seller) {
        super(identifier, name, description, price, status, seller);
    }

    @Override
    public boolean requiresPhysicalDispatch() {
        return true;
    }
}
