package application.domain.services.logistics;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Shipment;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ShipmentTrackingRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;

import java.util.List;

/**
 * ConsultShipmentService
 *
 * Catalog service "Consult Shipment". Retrieves
 * the shipment of an order and its tracking history (oldest event first)
 * for the requesters that may access that order.
 */
public class ConsultShipmentService {

    private final OrderRepository orderRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public ConsultShipmentService(OrderRepository orderRepository,
                                  ShipmentTrackingRepository trackingRepository,
                                  ValidatePermissionsService permissionsService,
                                  ValidateOwnershipAccessService ownershipService) {
        if (orderRepository == null || trackingRepository == null || permissionsService == null
                || ownershipService == null) {
            throw new IllegalArgumentException("ConsultShipmentService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.trackingRepository = trackingRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    public Shipment consultShipment(String requesterId, String orderId) {
        return requireShipment(findAccessibleOrder(requesterId, orderId));
    }

    public List<ShipmentTrackingEvent> consultTracking(String requesterId, String orderId) {
        Shipment shipment = requireShipment(findAccessibleOrder(requesterId, orderId));
        return trackingRepository.findByShipmentId(shipment.getShipmentId());
    }

    private Order findAccessibleOrder(String requesterId, String orderId) {
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.CONSULT_SHIPMENT);
        ServiceValidations.requireText(orderId, "order id");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        ownershipService.requireOrderAccess(requester, order);
        return order;
    }

    private Shipment requireShipment(Order order) {
        if (order.getShipment() == null) {
            throw new IllegalArgumentException("Order '" + order.getOrderId() + "' has no shipment");
        }
        return order.getShipment();
    }
}
