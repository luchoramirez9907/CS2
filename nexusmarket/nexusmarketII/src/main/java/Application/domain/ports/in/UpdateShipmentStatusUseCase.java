package Application.domain.ports.in;

import Application.domain.models.Shipment;

/**
 * Input port (use case): Update Shipment Status.
 *
 * Registers the physical dispatch of a shipment from the originating
 * warehouse and its subsequent delivery confirmation to the buyer.
 */
public interface UpdateShipmentStatusUseCase {

    /** Registers the dispatch of the shipment of the given order. */
    Shipment dispatchShipment(String operatorId, String orderId);

    /** Registers the delivery confirmation of the shipment of the given order. */
    Shipment confirmDelivery(String operatorId, String orderId);
}
