package Application.domain.services;

import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.models.Invoice;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.ports.in.ConsultInvoiceUseCase;
import Application.domain.ports.in.GenerateInvoiceUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.valueobjects.OrderStatus;
import Application.domain.valueobjects.SystemRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BillingService
 *
 * Implements the Billing Management services (per SDD - Services):
 * - Generate Invoice: creates the billing record of a confirmed order,
 *   including the applicable total and tax amounts.
 * - Consult Invoice: retrieves the invoice of an order according to the
 *   access permissions of the requesting user.
 */
public class BillingService implements GenerateInvoiceUseCase, ConsultInvoiceUseCase {

    private final PersonRepository personRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public BillingService(PersonRepository personRepository,
                          OrderRepository orderRepository,
                          NotificationService notificationService) {
        if (personRepository == null || orderRepository == null || notificationService == null) {
            throw new IllegalArgumentException("BillingService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Invoice generateInvoice(String performerId, String orderId, BigDecimal taxRate) {
        requireText(performerId, "performer id");
        requireText(orderId, "order id");
        if (taxRate == null || taxRate.signum() < 0) {
            throw new IllegalArgumentException("Tax rate must not be null or negative");
        }

        Person performer = requireActivePerson(performerId);
        performer.requireRole("generate an invoice", SystemRole.ADMINISTRATOR);

        Order order = requireOrder(orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT
                && order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Order '" + orderId
                    + "' must be confirmed before invoicing (current: "
                    + order.getOrderStatus().getCode() + ")");
        }
        if (order.getInvoice() != null) {
            throw new IllegalStateException("Order '" + orderId + "' already has an invoice");
        }

        Invoice invoice = Invoice.issueFor(order, UUID.randomUUID().toString(),
                LocalDateTime.now(), taxRate);
        order.attachInvoice(invoice);
        orderRepository.save(order);
        notificationService.notify(order.getBuyer(), "Invoice generated",
                "Invoice " + invoice.getInvoiceId() + " was issued for order " + orderId);
        return invoice;
    }

    @Override
    public Invoice consultInvoice(String requesterId, String orderId) {
        requireText(requesterId, "requester id");
        requireText(orderId, "order id");

        Order order = requireOrder(orderId);
        Person requester = requireActivePerson(requesterId);

        boolean isOwner = order.getBuyer().getIdentifier().equals(requesterId);
        if (!isOwner && requester.getRole() != SystemRole.ADMINISTRATOR
                && requester.getRole() != SystemRole.SUPERVISOR) {
            throw new InvalidRoleAssignmentException("consult an invoice", requester.getRole(),
                    "the buyer who placed the order, an ADMINISTRATOR or a SUPERVISOR");
        }
        if (order.getInvoice() == null) {
            throw new IllegalStateException("Order '" + orderId + "' has no invoice yet");
        }
        return order.getInvoice();
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
