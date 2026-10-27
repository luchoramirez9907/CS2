package application.domain.services.logistics;

import application.domain.enums.BusinessOperation;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.TrackingEventType;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Person;
import application.domain.models.Shipment;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ShipmentTrackingRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.SystemRole;

import java.time.LocalDateTime;

/**
 * UpdateShipmentStatusService
 *
 * Catalog service "Update Shipment Status":
 * dispatch, delivery confirmation and failed delivery of a shipment,
 * recording every step in the shipment tracking history. A logistics
 * operator only updates the shipments assigned to it.
 *
 * Dispatching turns the reserved stock into a completed sale (SALE_EXIT
 * movement). A returned shipment (failed delivery) can be dispatched again.
 * Delivery confirmation does not finalize the order: that is done afterward
 * through UpdateOrderStatusService#finalizeOrder.
 */
public class UpdateShipmentStatusService {

    private final OrderRepository orderRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final InventoryReservationService inventoryReservationService;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;
    private final NotificationService notificationService;

    public UpdateShipmentStatusService(OrderRepository orderRepository,
                                       ShipmentTrackingRepository trackingRepository,
                                       InventoryReservationService inventoryReservationService,
                                       ValidatePermissionsService permissionsService,
                                       ValidateOwnershipAccessService ownershipService,
                                       NotificationService notificationService) {
        if (orderRepository == null || trackingRepository == null || inventoryReservationService == null
                || permissionsService == null || ownershipService == null || notificationService == null) {
            throw new IllegalArgumentException("UpdateShipmentStatusService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.trackingRepository = trackingRepository;
        this.inventoryReservationService = inventoryReservationService;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
        this.notificationService = notificationService;
    }

    /**
     * Dispatches a pending shipment: the order leaves the warehouse and the
     * reserved stock becomes a completed sale (SALE_EXIT movement). A
     * returned shipment is dispatched again without any order transition
     * nor stock movement.
     */
    public Shipment dispatchShipment(String requesterId, String orderId) {
        Person performer = permissionsService.requirePermission(requesterId,
                BusinessOperation.UPDATE_SHIPMENT_STATUS);
        Order order = findOrder(orderId);
        Shipment shipment = requireShipment(order);
        requireOperatorAccess(performer, order);

        if (shipment.getShipmentStatus() == ShipmentStatus.RETURNED) {
            // Failed delivery: the goods left the warehouse once (order SHIPPED, stock
            // already recorded as sold), so the shipment is simply dispatched again.
            shipment.redispatch();
            orderRepository.save(order);
            trackingRepository.record(shipment.getShipmentId(), orderId, TrackingEventType.DISPATCHED,
                    LocalDateTime.now(), "Order dispatched again after a returned delivery");
            notificationService.notify(order.getBuyer(), "Order shipped again",
                    "Order " + orderId + " is on its way again");
            return shipment;
        }
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Order '" + orderId
                    + "' must be PAID to enter the logistics process (current: "
                    + order.getOrderStatus().getCode() + ")");
        }

        // Validate every reservation first: a failed dispatch must leave the order untouched.
        for (OrderItem item : order.getItems()) {
            if (item.getProduct().requiresPhysicalDispatch()) {
                inventoryReservationService.requireReservedStock(item.getProduct(), item.getQuantity());
            }
        }
        shipment.dispatch();
        for (OrderItem item : order.getItems()) {
            if (item.getProduct().requiresPhysicalDispatch()) {
                inventoryReservationService.confirmSale(item.getProduct(), item.getQuantity(), performer);
            }
        }
        orderRepository.save(order);
        trackingRepository.record(shipment.getShipmentId(), orderId, TrackingEventType.DISPATCHED, LocalDateTime.now(),
                "Order dispatched by operator " + shipment.getLogisticsOperator().getFullName());
        notificationService.notify(order.getBuyer(), "Order shipped",
                "Order " + orderId + " left the warehouse and is on its way");
        return shipment;
    }

    /**
     * Confirms the delivery of an in-transit shipment to the buyer.
     */
    public Shipment confirmDelivery(String requesterId, String orderId) {
        Person performer = permissionsService.requirePermission(requesterId,
                BusinessOperation.UPDATE_SHIPMENT_STATUS);
        Order order = findOrder(orderId);
        Shipment shipment = requireShipment(order);
        requireOperatorAccess(performer, order);

        shipment.markDelivered();
        orderRepository.save(order);
        trackingRepository.record(shipment.getShipmentId(), orderId, TrackingEventType.DELIVERED, LocalDateTime.now(),
                "Shipment delivered to the buyer");
        notificationService.notify(order.getBuyer(), "Shipment delivered",
                "The shipment of order " + orderId + " was delivered");
        return shipment;
    }

    /**
     * Registers a failed delivery: the in-transit shipment returns to the
     * warehouse and can be dispatched again.
     */
    public Shipment registerShipmentReturn(String requesterId, String orderId, String reason) {
        ServiceValidations.requireText(reason, "return reason");
        Person performer = permissionsService.requirePermission(requesterId,
                BusinessOperation.UPDATE_SHIPMENT_STATUS);
        Order order = findOrder(orderId);
        Shipment shipment = requireShipment(order);
        requireOperatorAccess(performer, order);

        shipment.markReturned();
        orderRepository.save(order);
        trackingRepository.record(shipment.getShipmentId(), orderId, TrackingEventType.RETURNED,
                LocalDateTime.now(), "Shipment returned to the warehouse: " + reason);
        notificationService.notify(order.getBuyer(), "Delivery failed",
                "The shipment of order " + orderId + " returned to the warehouse: " + reason);
        return shipment;
    }

    /**
     * A logistics operator only updates the shipments assigned to it.
     */
    private void requireOperatorAccess(Person performer, Order order) {
        if (performer.getRole() == SystemRole.LOGISTICS_OPERATOR) {
            ownershipService.requireOrderAccess(performer, order);
        }
    }

    private Shipment requireShipment(Order order) {
        if (order.getShipment() == null) {
            throw new IllegalArgumentException("Order '" + order.getOrderId() + "' has no shipment");
        }
        return order.getShipment();
    }

    private Order findOrder(String orderId) {
        ServiceValidations.requireText(orderId, "order id");
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
    }
}
