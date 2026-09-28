package Application.domain.services;

import Application.domain.models.LogisticsOperator;
import Application.domain.models.Order;
import Application.domain.models.OrderItem;
import Application.domain.models.Person;
import Application.domain.models.Shipment;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ConsultShipmentUseCase;
import Application.domain.ports.in.CreateShipmentUseCase;
import Application.domain.ports.in.UpdateShipmentStatusUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ShipmentTrackingRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.valueobjects.SystemRole;

import java.time.LocalDateTime;

/**
 * ShipmentDispatchService
 *
 * Implements the Logistics Management services (per SDD - Services):
 * - Create Shipment: creates the shipment record of a paid order from an
 *   originating warehouse, assigning the responsible logistics operator.
 * - Update Shipment Status: registers the physical dispatch and the
 *   delivery confirmation of a shipment.
 * - Consult Shipment: retrieves shipment status by permissions.
 *
 * Every step is recorded in the shipment tracking history.
 */
public class ShipmentDispatchService implements CreateShipmentUseCase, UpdateShipmentStatusUseCase, ConsultShipmentUseCase {

    private final OrderRepository orderRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final InventoryReservationService inventoryReservationService;
    private final NotificationService notificationService;
    private final PersonRepository personRepository;
    private final WarehouseRepository warehouseRepository;

    public ShipmentDispatchService(OrderRepository orderRepository,
                                   ShipmentTrackingRepository trackingRepository,
                                   InventoryReservationService inventoryReservationService,
                                   NotificationService notificationService,
                                   PersonRepository personRepository,
                                   WarehouseRepository warehouseRepository) {
        if (orderRepository == null || trackingRepository == null
                || inventoryReservationService == null || notificationService == null
                || personRepository == null || warehouseRepository == null) {
            throw new IllegalArgumentException("ShipmentDispatchService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.trackingRepository = trackingRepository;
        this.inventoryReservationService = inventoryReservationService;
        this.notificationService = notificationService;
        this.personRepository = personRepository;
        this.warehouseRepository = warehouseRepository;
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

    @Override
    public Shipment createShipment(String operatorId, String orderId, String originWarehouseId) {
        requireText(operatorId, "operator id");
        requireText(orderId, "order id");
        requireText(originWarehouseId, "origin warehouse id");

        LogisticsOperator operator = requireLogisticsOperator(operatorId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order '" + orderId + "' does not exist"));
        Warehouse warehouse = warehouseRepository.findById(originWarehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse '" + originWarehouseId + "' does not exist"));

        if (order.getItems().stream().noneMatch(item -> item.getProduct().requiresPhysicalDispatch())) {
            throw new IllegalStateException("Order '" + orderId
                    + "' contains no physical products; a shipment is not required");
        }

        Shipment shipment = createShipment(order, warehouse, operator);
        operator.assignShipment(shipment);
        personRepository.save(operator);
        return shipment;
    }

    @Override
    public Shipment dispatchShipment(String operatorId, String orderId) {
        Shipment shipment = requireAssignedShipment(operatorId, orderId);
        dispatch(shipment, shipment.getLogisticsOperator());
        return shipment;
    }

    @Override
    public Shipment confirmDelivery(String operatorId, String orderId) {
        Shipment shipment = requireAssignedShipment(operatorId, orderId);
        markDelivered(shipment);
        return shipment;
    }

    @Override
    public Shipment consultShipment(String requesterId, String orderId) {
        requireText(requesterId, "requester id");
        requireText(orderId, "order id");

        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order '" + orderId + "' does not exist"));
        Shipment shipment = requireShipment(order);

        boolean isOwner = order.getBuyer().getIdentifier().equals(requesterId);
        boolean isAssignedOperator = shipment.getLogisticsOperator().getIdentifier().equals(requesterId);
        if (!isOwner && !isAssignedOperator
                && requester.getRole() != SystemRole.ADMINISTRATOR
                && requester.getRole() != SystemRole.SUPERVISOR) {
            throw new Application.domain.exceptions.InvalidRoleAssignmentException(
                    "consult a shipment", requester.getRole(),
                    "the buyer, the assigned logistics operator, an ADMINISTRATOR or a SUPERVISOR");
        }
        return shipment;
    }

    private Shipment requireAssignedShipment(String operatorId, String orderId) {
        LogisticsOperator operator = requireLogisticsOperator(operatorId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order '" + orderId + "' does not exist"));
        Shipment shipment = requireShipment(order);
        if (!shipment.getLogisticsOperator().getIdentifier().equals(operatorId)) {
            throw new Application.domain.exceptions.InvalidRoleAssignmentException(
                    "operate the shipment of order " + orderId, operator.getRole(),
                    "the assigned LOGISTICS_OPERATOR");
        }
        return shipment;
    }

    private Shipment requireShipment(Order order) {
        Shipment shipment = order.getShipment();
        if (shipment == null) {
            throw new IllegalStateException("Order '" + order.getOrderId() + "' has no shipment yet");
        }
        return shipment;
    }

    private LogisticsOperator requireLogisticsOperator(String operatorId) {
        Person person = personRepository.findById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Operator '" + operatorId + "' does not exist"));
        person.requireActive();
        person.requireRole("operate shipments", SystemRole.LOGISTICS_OPERATOR);
        return (LogisticsOperator) person;
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
