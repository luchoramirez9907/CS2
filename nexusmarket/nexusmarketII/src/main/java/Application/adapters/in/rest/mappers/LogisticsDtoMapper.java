package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.InvoiceResponse;
import Application.adapters.in.rest.responses.ShipmentResponse;
import Application.adapters.in.rest.responses.TrackingEventResponse;
import Application.domain.models.Invoice;
import Application.domain.models.Shipment;
import Application.domain.models.ShipmentTrackingEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper: Domain Models (Invoice, Shipment, tracking events) to Response DTOs.
 */
public final class LogisticsDtoMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private LogisticsDtoMapper() {
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getInvoiceId(),
                invoice.getOrder().getOrderId(),
                format(invoice.getIssueDate()),
                invoice.getTotalAmount(),
                invoice.getTaxAmount());
    }

    public static ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getShipmentId(),
                shipment.getOrder().getOrderId(),
                shipment.getShipmentStatus().name(),
                shipment.getLogisticsOperator().getIdentifier(),
                shipment.getOriginWarehouse().getIdentifier(),
                PersonDtoMapper.toResponse(shipment.getShippingAddress()),
                format(shipment.getDispatchDate()),
                format(shipment.getDeliveryDate()));
    }

    public static TrackingEventResponse toResponse(ShipmentTrackingEvent event) {
        return new TrackingEventResponse(event.event(), format(event.occurredAt()), event.details());
    }

    private static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(ISO);
    }
}
