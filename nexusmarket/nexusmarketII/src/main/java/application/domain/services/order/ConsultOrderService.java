package application.domain.services.order;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.ports.out.OrderRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.OrderStatus;

import java.util.Comparator;
import java.util.List;

/**
 * ConsultOrderService
 *
 * Catalog service "Consult Order". Retrieves and
 * lists orders according to the access permissions of the requesting user:
 * all orders for Administrators and Supervisors, own orders for a Buyer,
 * orders containing its products for a Seller, and orders whose shipment
 * is assigned to it for a Logistics Operator.
 */
public class ConsultOrderService {

    private final OrderRepository orderRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public ConsultOrderService(OrderRepository orderRepository,
                               ValidatePermissionsService permissionsService,
                               ValidateOwnershipAccessService ownershipService) {
        if (orderRepository == null || permissionsService == null || ownershipService == null) {
            throw new IllegalArgumentException("ConsultOrderService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    public Order consultOrder(String requesterId, String orderId) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_ORDER);
        ServiceValidations.requireText(orderId, "order id");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        ownershipService.requireOrderAccess(requester, order);
        return order;
    }

    /**
     * Lists the orders the requester may access (all for Administrators and
     * Supervisors; own orders for a Buyer; orders containing its products for
     * a Seller; orders whose shipment is assigned to it for a Logistics
     * Operator), newest first.
     *
     * @param status optional status filter
     */
    public List<Order> listOrders(String requesterId, OrderStatus status) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_ORDER);
        return orderRepository.findAll().stream()
                .filter(order -> status == null || order.getOrderStatus().equals(status))
                .filter(order -> ownershipService.canAccessOrder(requester, order))
                .sorted(Comparator.comparing(Order::getCreationDate).reversed())
                .toList();
    }
}
