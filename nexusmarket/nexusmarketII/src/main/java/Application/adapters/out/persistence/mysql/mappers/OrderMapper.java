package Application.adapters.out.persistence.mysql.mappers;

import Application.adapters.out.persistence.mysql.entities.OrderEntity;
import Application.adapters.out.persistence.mysql.entities.OrderItemEntity;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.OrderItem;
import Application.domain.models.Product;
import Application.domain.valueobjects.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Mapper: Order domain models to JPA entities and back.
 */
public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderId(order.getOrderId());
        applyOrder(entity, order);
        return entity;
    }

    public static void updateEntity(OrderEntity entity, Order order) {
        applyOrder(entity, order);
    }

    public static Order toDomain(OrderEntity entity, Buyer buyer,
                                 Function<String, Product> productResolver) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemEntity itemEntity : entity.getItems()) {
            Product product = productResolver.apply(itemEntity.getProductId());
            items.add(new OrderItem(product, itemEntity.getQuantity(), itemEntity.getUnitPrice()));
        }
        Order.InvoiceSnapshot invoiceSnapshot = entity.getInvoiceId() == null ? null
                : new Order.InvoiceSnapshot(entity.getInvoiceId(), entity.getInvoiceIssueDate(),
                        entity.getInvoiceTotalAmount(), entity.getInvoiceTaxAmount());
        return Order.reconstruct(entity.getOrderId(), buyer, items,
                OrderStatus.fromCode(entity.getOrderStatusCode()), entity.getCreationDate(),
                invoiceSnapshot);
    }

    private static void applyOrder(OrderEntity entity, Order order) {
        entity.setBuyerId(order.getBuyer().getIdentifier());
        entity.setOrderStatusCode(order.getOrderStatus().getCode());
        entity.setCreationDate(order.getCreationDate());
        entity.setTotalAmount(order.getTotalAmount());
        if (order.getInvoice() != null) {
            entity.setInvoiceId(order.getInvoice().getInvoiceId());
            entity.setInvoiceIssueDate(order.getInvoice().getIssueDate());
            entity.setInvoiceTotalAmount(order.getInvoice().getTotalAmount());
            entity.setInvoiceTaxAmount(order.getInvoice().getTaxAmount());
        }
        entity.getItems().clear();
        for (OrderItem item : order.getItems()) {
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setProductId(item.getProduct().getIdentifier());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitPrice(item.getUnitPrice());
            itemEntity.setSubtotal(item.getSubtotal());
            itemEntity.setOrder(entity);
            entity.getItems().add(itemEntity);
        }
    }
}
