package application.domain.services.logistics;

import application.domain.enums.BusinessOperation;
import application.domain.enums.TrackingEventType;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.LogisticsOperator;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Shipment;
import application.domain.models.Warehouse;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.PersonRepository;
import application.domain.ports.out.ShipmentTrackingRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CreateShipmentService
 *
 * Catalog service "Create Shipment". Creates the
 * shipment of a paid order that contains physical products, from an origin
 * warehouse to the buyer's primary address, and assigns the responsible
 * logistics operator. Shipments are addressed by the order they fulfill
 * (one shipment per order).
 */
public class CreateShipmentService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final PersonRepository personRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final ValidatePermissionsService permissionsService;
    private final NotificationService notificationService;

    public CreateShipmentService(OrderRepository orderRepository,
                                 WarehouseRepository warehouseRepository,
                                 PersonRepository personRepository,
                                 ShipmentTrackingRepository trackingRepository,
                                 ValidatePermissionsService permissionsService,
                                 NotificationService notificationService) {
        if (orderRepository == null || warehouseRepository == null || personRepository == null
                || trackingRepository == null || permissionsService == null || notificationService == null) {
            throw new IllegalArgumentException("CreateShipmentService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
        this.personRepository = personRepository;
        this.trackingRepository = trackingRepository;
        this.permissionsService = permissionsService;
        this.notificationService = notificationService;
    }

    public Shipment createShipment(String requesterId, String orderId, String originWarehouseId,
                                   String logisticsOperatorId) {
        permissionsService.requirePermission(requesterId, BusinessOperation.CREATE_SHIPMENT);
        ServiceValidations.requireText(orderId, "order id");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Order '" + orderId
                    + "' must be PAID to enter the logistics process (current: "
                    + order.getOrderStatus().getCode() + ")");
        }
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
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", originWarehouseId));
        LogisticsOperator operator = findOperator(logisticsOperatorId);

        Shipment shipment = new Shipment(UUID.randomUUID().toString(), order, operator,
                originWarehouse, order.getBuyer().getPrimaryAddress());
        order.attachShipment(shipment);
        operator.assignShipment(shipment);
        orderRepository.save(order);
        trackingRepository.record(shipment.getShipmentId(), orderId, TrackingEventType.CREATED, LocalDateTime.now(),
                "Shipment created at warehouse " + originWarehouse.getName()
                        + ", assigned to operator " + operator.getFullName());
        notificationService.notify(operator, "Shipment assigned",
                "Shipment of order " + orderId + " was assigned to you");
        return shipment;
    }

    private LogisticsOperator findOperator(String operatorId) {
        ServiceValidations.requireText(operatorId, "logistics operator id");
        Person person = personRepository.findById(operatorId)
                .orElseThrow(() -> new EntityNotFoundException("Logistics operator", operatorId));
        if (!(person instanceof LogisticsOperator operator)) {
            throw new IllegalArgumentException("Person '" + operatorId + "' is not a logistics operator");
        }
        operator.requireActive();
        return operator;
    }
}
