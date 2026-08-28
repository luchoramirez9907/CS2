package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.OrderResponse;
import Application.domain.models.Order;
import Application.domain.models.OrderItem;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper: Domain Model (Order) to Response DTO.
 */
public final class OrderDtoMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private OrderDtoMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderResponse.ItemResponse> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            items.add(new OrderResponse.ItemResponse(
                    item.getProduct().getIdentifier(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
        }
        return new OrderResponse(
                order.getOrderId(),
                order.getBuyer().getIdentifier(),
                order.getOrderStatus().getCode(),
                order.getCreationDate().format(ISO),
                order.getTotalAmount(),
                items,
                order.getInvoice() == null ? null : order.getInvoice().getInvoiceId(),
                order.getInvoice() == null ? null : order.getInvoice().getTaxAmount(),
                order.getShipment() != null);
    }
}
