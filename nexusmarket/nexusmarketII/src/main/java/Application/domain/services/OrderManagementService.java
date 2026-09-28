package Application.domain.services;

import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.ports.in.ConsultOrderUseCase;
import Application.domain.ports.in.UpdateOrderStatusUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.valueobjects.OrderStatus;
import Application.domain.valueobjects.SystemRole;

/**
 * OrderManagementService
 *
 * Implements the Order Management services (per SDD - Services):
 * - Update Order Status: registers the financial confirmation of an
 *   order (PENDING_PAYMENT to PAID). Finalization happens automatically
 *   once the associated shipment is delivered (Shipment model).
 * - Consult Order: retrieves order information according to the access
 *   permissions of the requesting user.
 */
public class OrderManagementService implements UpdateOrderStatusUseCase, ConsultOrderUseCase {

    private final PersonRepository personRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public OrderManagementService(PersonRepository personRepository,
                                  OrderRepository orderRepository,
                                  NotificationService notificationService) {
        if (personRepository == null || orderRepository == null || notificationService == null) {
            throw new IllegalArgumentException("OrderManagementService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Order confirmPayment(String performerId, String orderId) {
        requireText(performerId, "performer id");
        requireText(orderId, "order id");

        Order order = requireOrder(orderId);
        Person performer = requireActivePerson(performerId);

        boolean ownerConfirming = order.getBuyer().getIdentifier().equals(performerId);
        if (!ownerConfirming && performer.getRole() != SystemRole.ADMINISTRATOR) {
            throw new InvalidRoleAssignmentException("confirm the payment of an order",
                    performer.getRole(),
                    "the buyer who placed the order or an ADMINISTRATOR");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Order '" + orderId
                    + "' must be PENDING_PAYMENT to confirm payment (current: "
                    + order.getOrderStatus().getCode() + ")");
        }

        order.markPaid();
        orderRepository.save(order);
        notificationService.notify(order.getBuyer(), "Payment confirmed",
                "Order " + orderId + " is confirmed and enters preparation");
        return order;
    }

    @Override
    public Order consultOrder(String requesterId, String orderId) {
        requireText(requesterId, "requester id");
        requireText(orderId, "order id");

        Order order = requireOrder(orderId);
        Person requester = requireActivePerson(requesterId);

        boolean isOwner = order.getBuyer().getIdentifier().equals(requesterId);
        if (!isOwner && requester.getRole() != SystemRole.ADMINISTRATOR
                && requester.getRole() != SystemRole.SUPERVISOR) {
            throw new InvalidRoleAssignmentException("consult an order", requester.getRole(),
                    "the buyer who placed the order, an ADMINISTRATOR or a SUPERVISOR");
        }
        return order;
    }

    private Order requireOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order '" + orderId + "' does not exist"));
    }

    private Person requireActivePerson(String personId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Person '" + personId + "' does not exist"));
        person.requireActive();
        return person;
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
