package Application.domain.ports.in;

import Application.domain.models.Shipment;

/**
 * Input port (use case): Consult Shipment.
 *
 * Retrieves the current status and tracking information of a shipment
 * according to the access permissions of the requesting user.
 */
public interface ConsultShipmentUseCase {

    Shipment consultShipment(String requesterId, String orderId);
}
