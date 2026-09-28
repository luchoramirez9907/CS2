package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.ports.in.ConsultOrderUseCase;
import Application.domain.ports.in.UpdateOrderStatusUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.valueobjects.SystemRole;

/**
 * OrderManagementService
 *
 * Implements UpdateOrderStatusUseCase and ConsultOrderUseCase. Every
 * status transition is a significant business event validated by the
 * Order lifecycle (CART -> PENDING_PAYMENT -> PAID -> SHIPPED ->
 * DELIVERED); a finalized order cannot be modified afterward. Order
 * confirmation (checkout) is handled by OrderCheckoutService.
 */
public class OrderManagementService implements UpdateOrderStatusUseCase, ConsultOrderUseCase {

    private final OrderRepository orderRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public OrderManagementService(OrderRepository orderRepository,
                                  AuthorizationService authorizationService,
                                  NotificationService notificationService) {
        if (orderRepository == null || authorizationService == null || notificationService == null) {
            throw new IllegalArgumentException("OrderManagementService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Override
    public Order confirmPayment(String requesterId, String orderId) {
        authorizationService.requirePermission(requesterId, BusinessOperation.CONFIRM_ORDER_PAYMENT);
        Order order = findOrder(orderId);
        order.registerPayment();
        orderRepository.save(order);

        String message = order.requiresShipment()
                ? "Payment of order " + orderId + " was confirmed; preparation may begin"
                : "Payment of order " + orderId + " was confirmed and your digital products were delivered";
        notificationService.notify(order.getBuyer(), "Payment confirmed", message);
        return order;
    }

    @Override
    public Order finalizeOrder(String requesterId, String orderId) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.FINALIZE_ORDER);
        Order order = findOrder(orderId);
        if (requester.getRole() == SystemRole.LOGISTICS_OPERATOR) {
            authorizationService.requireOrderAccess(requester, order);
        }
        order.finalizeOrder();
        orderRepository.save(order);
        notificationService.notify(order.getBuyer(), "Order delivered",
                "Order " + orderId + " was delivered successfully and is now finalized");
        return order;
    }

    @Override
    public Order consultOrder(String requesterId, String orderId) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.CONSULT_ORDER);
        Order order = findOrder(orderId);
        authorizationService.requireOrderAccess(requester, order);
        return order;
    }

    private Order findOrder(String orderId) {
        ServiceValidations.requireText(orderId, "order id");
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order '" + orderId + "' does not exist"));
    }
}
