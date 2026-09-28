package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.ShipmentResponse;
import Application.domain.models.Shipment;

import java.time.format.DateTimeFormatter;

/**
 * Mapper: Domain Model (Shipment) to Response DTO.
 */
public final class ShipmentDtoMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ShipmentDtoMapper() {
    }

    public static ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getShipmentId(),
                shipment.getOrder().getOrderId(),
                shipment.getLogisticsOperator().getIdentifier(),
                shipment.getOriginWarehouse().getName(),
                shipment.getShipmentStatus().name(),
                shipment.getDispatchDate() == null ? null : shipment.getDispatchDate().format(ISO),
                shipment.getDeliveryDate() == null ? null : shipment.getDeliveryDate().format(ISO));
    }
}
