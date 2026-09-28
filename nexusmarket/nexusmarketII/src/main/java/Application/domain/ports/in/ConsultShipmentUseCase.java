package Application.domain.ports.in;

import Application.domain.models.Shipment;
import Application.domain.models.ShipmentTrackingEvent;

import java.util.List;

/**
 * Input port (use case): retrieves the current status and tracking
 * information of the shipment of an order.
 */
public interface ConsultShipmentUseCase {

    Shipment consultShipment(String requesterId, String orderId);

    List<ShipmentTrackingEvent> consultTracking(String requesterId, String orderId);
}
