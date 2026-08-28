package Application.domain.models;

import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LogisticsOperator
 *
 * Represents the participant responsible for the physical operation of
 * warehouses and the dispatch of orders.
 */
public class LogisticsOperator extends Person {

    private final List<Shipment> shipments = new ArrayList<>();

    public LogisticsOperator(String identifier, String fullName, String email, UserStatus status) {
        super(identifier, fullName, email, SystemRole.LOGISTICS_OPERATOR, status);
    }

    public List<Shipment> getShipments() {
        return Collections.unmodifiableList(shipments);
    }

    public void assignShipment(Shipment shipment) {
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment must not be null");
        }
        if (!this.equals(shipment.getLogisticsOperator())) {
            throw new IllegalArgumentException("Shipment is not assigned to this operator");
        }
        this.shipments.add(shipment);
    }
}
