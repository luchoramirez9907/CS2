package Application.domain.ports.in;

import Application.domain.models.Shipment;

/**
 * Input port (use case): creates the shipment of a paid order, setting
 * the originating warehouse, the destination address (buyer's primary
 * address) and the responsible logistics operator.
 */
public interface CreateShipmentUseCase {

    Shipment createShipment(String requesterId, String orderId, String originWarehouseId,
                            String logisticsOperatorId);
}
