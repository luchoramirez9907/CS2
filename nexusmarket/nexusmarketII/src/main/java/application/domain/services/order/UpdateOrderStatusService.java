package application.domain.services.order;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.OrderRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.SystemRole;

/**
 * UpdateOrderStatusService
 *
 * Catalog service "Update Order Status". Every
 * status transition is a significant business event validated by the Order
 * lifecycle (CART -> PENDING_PAYMENT -> PAID -> SHIPPED -> DELIVERED); a
 * finalized order cannot be modified afterward. Order confirmation
 * (checkout) is handled by ConfirmOrderService.
 */
public class UpdateOrderStatusService {

    private final OrderRepository orderRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;
    private final NotificationService notificationService;

    public UpdateOrderStatusService(OrderRepository orderRepository,
                                    ValidatePermissionsService permissionsService,
                                    ValidateOwnershipAccessService ownershipService,
                                    NotificationService notificationService) {
        if (orderRepository == null || permissionsService == null || ownershipService == null
                || notificationService == null) {
            throw new IllegalArgumentException("UpdateOrderStatusService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
        this.notificationService = notificationService;
    }

    /**
     * PENDING_PAYMENT -> PAID. Digital-only orders are delivered at once.
     */
    public Order confirmPayment(String requesterId, String orderId) {
        permissionsService.requirePermission(requesterId, BusinessOperation.CONFIRM_ORDER_PAYMENT);
        Order order = findOrder(orderId);
        order.registerPayment();
        orderRepository.save(order);

        String message = order.requiresShipment()
                ? "Payment of order " + orderId + " was confirmed; preparation may begin"
                : "Payment of order " + orderId + " was confirmed and your digital products were delivered";
        notificationService.notify(order.getBuyer(), "Payment confirmed", message);
        return order;
    }

    /**
     * SHIPPED -> DELIVERED, only once the shipment has been delivered.
     */
    public Order finalizeOrder(String requesterId, String orderId) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.FINALIZE_ORDER);
        Order order = findOrder(orderId);
        if (requester.getRole() == SystemRole.LOGISTICS_OPERATOR) {
            ownershipService.requireOrderAccess(requester, order);
        }
        order.finalizeOrder();
        orderRepository.save(order);
        notificationService.notify(order.getBuyer(), "Order delivered",
                "Order " + orderId + " was delivered successfully and is now finalized");
        return order;
    }

    private Order findOrder(String orderId) {
        ServiceValidations.requireText(orderId, "order id");
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
    }
}
