package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.LogisticsOperator;
import Application.domain.models.Order;
import Application.domain.models.OrderItem;
import Application.domain.models.Person;
import Application.domain.models.Shipment;
import Application.domain.models.ShipmentTrackingEvent;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ConsultShipmentUseCase;
import Application.domain.ports.in.CreateShipmentUseCase;
import Application.domain.ports.in.UpdateShipmentStatusUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ShipmentTrackingRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.valueobjects.OrderStatus;
import Application.domain.valueobjects.SystemRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ShipmentDispatchService
 *
 * Implements CreateShipmentUseCase, UpdateShipmentStatusUseCase and
 * ConsultShipmentUseCase: the logistics process of a paid physical
 * order (shipment creation, dispatch and delivery confirmation),
 * recording every step in the shipment tracking history. Shipments are
 * addressed by the order they fulfill (one shipment per order).
 *
 * Dispatching turns the reserved stock into a completed sale (SALE_EXIT
 * movement). Delivery confirmation does not finalize the order: that is
 * done afterward through UpdateOrderStatusUseCase#finalizeOrder.
 */
public class ShipmentDispatchService implements CreateShipmentUseCase, UpdateShipmentStatusUseCase,
        ConsultShipmentUseCase {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final PersonRepository personRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final InventoryReservationService inventoryReservationService;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public ShipmentDispatchService(OrderRepository orderRepository,
                                   WarehouseRepository warehouseRepository,
                                   PersonRepository personRepository,
                                   ShipmentTrackingRepository trackingRepository,
                                   InventoryReservationService inventoryReservationService,
                                   AuthorizationService authorizationService,
                                   NotificationService notificationService) {
        if (orderRepository == null || warehouseRepository == null || personRepository == null
                || trackingRepository == null || inventoryReservationService == null
                || authorizationService == null || notificationService == null) {
            throw new IllegalArgumentException("ShipmentDispatchService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
        this.personRepository = personRepository;
        this.trackingRepository = trackingRepository;
        this.inventoryReservationService = inventoryReservationService;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    /**
     * Creates the shipment of a paid order from an origin warehouse,
     * assigning a logistics operator. Only orders containing physical
     * products require a shipment.
     */
    @Override
    public Shipment createShipment(String requesterId, String orderId, String originWarehouseId,
                                   String logisticsOperatorId) {
        authorizationService.requirePermission(requesterId, BusinessOperation.CREATE_SHIPMENT);
        Order order = findOrder(orderId);
        requirePaidOrder(order);
        if (!order.requiresShipment()) {
            throw new IllegalArgumentException("Order '" + orderId
                    + "' only contains digital products and does not require a shipment");
        }
        if (order.getShipment() != null) {
            throw new IllegalArgumentException("Order '" + orderId + "' already has a shipment");
        }
        if (order.getBuyer().getPrimaryAddress() == null) {
            throw new IllegalArgumentException("Buyer of order '" + orderId + "' has no delivery address");
        }

        ServiceValidations.requireText(originWarehouseId, "origin warehouse id");
        Warehouse originWarehouse = warehouseRepository.findById(originWarehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse '" + originWarehouseId + "' does not exist"));
        LogisticsOperator operator = findOperator(logisticsOperatorId);

        Shipment shipment = new Shipment(UUID.randomUUID().toString(), order, operator,
                originWarehouse, order.getBuyer().getPrimaryAddress());
        order.attachShipment(shipment);
        operator.assignShipment(shipment);
        orderRepository.save(order);
        trackingRepository.record(shipment.getShipmentId(), orderId, "CREATED", LocalDateTime.now(),
                "Shipment created at warehouse " + originWarehouse.getName()
                        + ", assigned to operator " + operator.getFullName());
        notificationService.notify(operator, "Shipment assigned",
                "Shipment of order " + orderId + " was assigned to you");
        return shipment;
    }

    /**
     * Dispatches a pending shipment: the order leaves the warehouse and
     * the reserved stock becomes a completed sale (SALE_EXIT movement).
     */
    @Override
    public Shipment dispatchShipment(String requesterId, String orderId) {
        Person performer = authorizationService.requirePermission(requesterId,
                BusinessOperation.UPDATE_SHIPMENT_STATUS);
        Order order = findOrder(orderId);
        Shipment shipment = requireShipment(order);
        requireOperatorAccess(performer, order);
        requirePaidOrder(order);

        shipment.dispatch();
        for (OrderItem item : order.getItems()) {
            if (item.getProduct().requiresPhysicalDispatch()) {
                inventoryReservationService.confirmSale(item.getProduct(), item.getQuantity(), performer);
            }
        }
        orderRepository.save(order);
        trackingRepository.record(shipment.getShipmentId(), orderId, "DISPATCHED", LocalDateTime.now(),
                "Order dispatched by operator " + shipment.getLogisticsOperator().getFullName());
        notificationService.notify(order.getBuyer(), "Order shipped",
                "Order " + orderId + " left the warehouse and is on its way");
        return shipment;
    }

    /**
     * Confirms the delivery of an in-transit shipment to the buyer.
     */
    @Override
    public Shipment confirmDelivery(String requesterId, String orderId) {
        Person performer = authorizationService.requirePermission(requesterId,
                BusinessOperation.UPDATE_SHIPMENT_STATUS);
        Order order = findOrder(orderId);
        Shipment shipment = requireShipment(order);
        requireOperatorAccess(performer, order);

        shipment.markDelivered();
        orderRepository.save(order);
        trackingRepository.record(shipment.getShipmentId(), orderId, "DELIVERED", LocalDateTime.now(),
                "Shipment delivered to the buyer");
        notificationService.notify(order.getBuyer(), "Shipment delivered",
                "The shipment of order " + orderId + " was delivered");
        return shipment;
    }

    @Override
    public Shipment consultShipment(String requesterId, String orderId) {
        Order order = findAccessibleOrder(requesterId, orderId);
        return requireShipment(order);
    }

    @Override
    public List<ShipmentTrackingEvent> consultTracking(String requesterId, String orderId) {
        Order order = findAccessibleOrder(requesterId, orderId);
        Shipment shipment = requireShipment(order);
        return trackingRepository.findByShipmentId(shipment.getShipmentId());
    }

    private Order findAccessibleOrder(String requesterId, String orderId) {
        Person requester = authorizationService.requirePermission(requesterId,
                BusinessOperation.CONSULT_SHIPMENT);
        Order order = findOrder(orderId);
        authorizationService.requireOrderAccess(requester, order);
        return order;
    }

    /**
     * A logistics operator only updates the shipments assigned to it.
     */
    private void requireOperatorAccess(Person performer, Order order) {
        if (performer.getRole() == SystemRole.LOGISTICS_OPERATOR) {
            authorizationService.requireOrderAccess(performer, order);
        }
    }

    private LogisticsOperator findOperator(String operatorId) {
        ServiceValidations.requireText(operatorId, "logistics operator id");
        Person person = personRepository.findById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Logistics operator '" + operatorId + "' does not exist"));
        if (!(person instanceof LogisticsOperator operator)) {
            throw new IllegalArgumentException("Person '" + operatorId + "' is not a logistics operator");
        }
        operator.requireActive();
        return operator;
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
                .orElseThrow(() -> new IllegalArgumentException("Order '" + orderId + "' does not exist"));
    }

    private void requirePaidOrder(Order order) {
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Order '" + order.getOrderId()
                    + "' must be PAID to enter the logistics process (current: "
                    + order.getOrderStatus().getCode() + ")");
        }
    }
}
