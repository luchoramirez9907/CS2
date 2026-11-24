package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.OrderResponse;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.OrderItem;

import java.math.BigDecimal;
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
        return toResponse(order, order.getItems(), order.getTotalAmount(), order.getInvoice());
    }

    /**
     * Order as a seller sees it: only its own items and their total. An order
     * shared with other sellers hides the invoice, which covers the whole order.
     */
    public static OrderResponse toSellerResponse(Order order, String sellerId) {
        List<OrderItem> ownItems = order.getItemsOf(sellerId);
        boolean shared = ownItems.size() < order.getItems().size();
        BigDecimal ownTotal = ownItems.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return toResponse(order, ownItems, ownTotal, shared ? null : order.getInvoice());
    }

    private static OrderResponse toResponse(Order order, List<OrderItem> orderItems, BigDecimal totalAmount,
                                            Invoice invoice) {
        List<OrderResponse.ItemResponse> items = new ArrayList<>();
        for (OrderItem item : orderItems) {
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
                totalAmount,
                items,
                invoice == null ? null : invoice.getInvoiceId(),
                invoice == null ? null : invoice.getTaxAmount(),
                order.getShipment() != null);
    }
}
