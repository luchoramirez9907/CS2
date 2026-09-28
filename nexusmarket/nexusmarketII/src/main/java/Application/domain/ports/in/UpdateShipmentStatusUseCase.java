package Application.domain.ports.in;

import Application.domain.models.Shipment;

/**
 * Input port (use case): registers the physical dispatch of a shipment
 * and its subsequent delivery confirmation to the buyer.
 */
public interface UpdateShipmentStatusUseCase {

    Shipment dispatchShipment(String requesterId, String orderId);

    Shipment confirmDelivery(String requesterId, String orderId);
}
