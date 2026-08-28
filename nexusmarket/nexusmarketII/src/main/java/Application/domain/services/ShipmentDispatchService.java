package Application.domain.services;

import Application.domain.models.LogisticsOperator;
import Application.domain.models.Order;
import Application.domain.models.OrderItem;
import Application.domain.models.Person;
import Application.domain.models.Shipment;
import Application.domain.models.Warehouse;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.ShipmentTrackingRepository;

import java.time.LocalDateTime;

/**
 * ShipmentDispatchService
 *
 * Domain service coordinating the logistics process of a paid physical
 * order: shipment creation, dispatch and delivery, recording every step
 * in the shipment tracking history.
 */
public class ShipmentDispatchService {

    private final OrderRepository orderRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final InventoryReservationService inventoryReservationService;
    private final NotificationService notificationService;

    public ShipmentDispatchService(OrderRepository orderRepository,
                                   ShipmentTrackingRepository trackingRepository,
                                   InventoryReservationService inventoryReservationService,
                                   NotificationService notificationService) {
        if (orderRepository == null || trackingRepository == null
                || inventoryReservationService == null || notificationService == null) {
            throw new IllegalArgumentException("ShipmentDispatchService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.trackingRepository = trackingRepository;
        this.inventoryReservationService = inventoryReservationService;
        this.notificationService = notificationService;
    }

    /**
     * Creates the shipment of a paid order from an origin warehouse,
     * assigning a logistics operator. Only orders containing physical
     * products require a shipment.
     */
    public Shipment createShipment(Order order, Warehouse originWarehouse,
                                   LogisticsOperator operator) {
        requirePaidOrder(order);
        if (order.getShipment() != null) {
            throw new IllegalArgumentException("Order '" + order.getOrderId()
                    + "' already has a shipment");
        }

        Shipment shipment = new Shipment(java.util.UUID.randomUUID().toString(), order,
                operator, originWarehouse, order.getBuyer().getPrimaryAddress());
        order.attachShipment(shipment);
        orderRepository.save(order);
        trackingRepository.record(shipment.getShipmentId(), order.getOrderId(), "CREATED",
                LocalDateTime.now(), "Shipment created at warehouse " + originWarehouse.getName());
        return shipment;
    }

    /**
     * Dispatches a pending shipment: the order leaves the warehouse and
     * the reserved stock becomes a completed sale (SALE_EXIT movement).
     */
    public void dispatch(Shipment shipment, Person performedBy) {
        requirePaidOrder(shipment.getOrder());
        shipment.dispatch();
        for (OrderItem item : shipment.getOrder().getItems()) {
            if (item.getProduct().requiresPhysicalDispatch()) {
                inventoryReservationService.confirmSale(item.getProduct(), item.getQuantity(), performedBy);
            }
        }
        orderRepository.save(shipment.getOrder());
        trackingRepository.record(shipment.getShipmentId(), shipment.getOrder().getOrderId(),
                "DISPATCHED", LocalDateTime.now(),
                "Order dispatched by operator " + shipment.getLogisticsOperator().getFullName());
    }

    /**
     * Marks a shipment as delivered, completing the order lifecycle.
     */
    public void markDelivered(Shipment shipment) {
        shipment.markDelivered();
        orderRepository.save(shipment.getOrder());
        trackingRepository.record(shipment.getShipmentId(), shipment.getOrder().getOrderId(),
                "DELIVERED", LocalDateTime.now(), "Order delivered to the buyer");
        notificationService.notify(shipment.getOrder().getBuyer(), "Order delivered",
                "Order " + shipment.getOrder().getOrderId() + " was delivered successfully");
    }

    private void requirePaidOrder(Order order) {
        if (order.getOrderStatus() != Application.domain.valueobjects.OrderStatus.PAID) {
            throw new IllegalArgumentException("Order '" + order.getOrderId()
                    + "' must be PAID to enter the logistics process (current: "
                    + order.getOrderStatus().getCode() + ")");
        }
    }
}
