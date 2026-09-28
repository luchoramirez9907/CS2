package Application.domain.ports.in;

import Application.domain.models.Shipment;

/**
 * Input port (use case): Create Shipment.
 *
 * Creates the shipment record associated with a paid order, establishes
 * the originating warehouse and destination address, and assigns the
 * responsible logistics operator.
 */
public interface CreateShipmentUseCase {

    /**
     * @param operatorId       identifier of the LogisticsOperator in charge
     * @param orderId          identifier of the paid order to fulfill
     * @param originWarehouseId identifier of the originating warehouse
     * @return the created Shipment
     */
    Shipment createShipment(String operatorId, String orderId, String originWarehouseId);
}
