package Application.adapters.out.persistence.mysql.mappers;

import Application.adapters.out.persistence.mysql.entities.OrderEntity;
import Application.adapters.out.persistence.mysql.entities.OrderItemEntity;
import Application.domain.enums.ShipmentStatus;
import Application.domain.models.Buyer;
import Application.domain.models.LogisticsOperator;
import Application.domain.models.Order;
import Application.domain.models.OrderItem;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.Shipment;
import Application.domain.models.Warehouse;
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

    /**
     * @param personResolver    resolves the logistics operator of the shipment
     * @param warehouseResolver resolves the origin warehouse of the shipment
     */
    public static Order toDomain(OrderEntity entity, Buyer buyer,
                                 Function<String, Product> productResolver,
                                 Function<String, Person> personResolver,
                                 Function<String, Warehouse> warehouseResolver) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemEntity itemEntity : entity.getItems()) {
            Product product = productResolver.apply(itemEntity.getProductId());
            items.add(new OrderItem(product, itemEntity.getQuantity(), itemEntity.getUnitPrice()));
        }
        Order.InvoiceSnapshot invoiceSnapshot = entity.getInvoiceId() == null ? null
                : new Order.InvoiceSnapshot(entity.getInvoiceId(), entity.getInvoiceIssueDate(),
                        entity.getInvoiceTotalAmount(), entity.getInvoiceTaxAmount());
        Order order = Order.reconstruct(entity.getOrderId(), buyer, items,
                OrderStatus.fromCode(entity.getOrderStatusCode()), entity.getCreationDate(),
                invoiceSnapshot);
        if (entity.getShipmentId() != null) {
            Person operator = personResolver.apply(entity.getShipmentOperatorId());
            if (!(operator instanceof LogisticsOperator logisticsOperator)) {
                throw new IllegalStateException("Shipment '" + entity.getShipmentId()
                        + "' must be assigned to a LogisticsOperator");
            }
            Shipment.reconstruct(entity.getShipmentId(), order, logisticsOperator,
                    warehouseResolver.apply(entity.getShipmentOriginWarehouseId()),
                    PersonMapper.toAddress(entity.getShippingAddress()),
                    ShipmentStatus.valueOf(entity.getShipmentStatusCode()),
                    entity.getShipmentDispatchDate(), entity.getShipmentDeliveryDate());
        }
        return order;
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
        Shipment shipment = order.getShipment();
        if (shipment != null) {
            entity.setShipmentId(shipment.getShipmentId());
            entity.setShipmentOperatorId(shipment.getLogisticsOperator().getIdentifier());
            entity.setShipmentOriginWarehouseId(shipment.getOriginWarehouse().getIdentifier());
            entity.setShippingAddress(PersonMapper.toEmbeddable(shipment.getShippingAddress()));
            entity.setShipmentStatusCode(shipment.getShipmentStatus().name());
            entity.setShipmentDispatchDate(shipment.getDispatchDate());
            entity.setShipmentDeliveryDate(shipment.getDeliveryDate());
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
