package Application.domain.services;

import Application.domain.exceptions.DuplicateEmailException;
import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.Return;
import Application.domain.ports.in.ManageBuyerUseCase;
import Application.domain.ports.in.RegisterBuyerUseCase;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ReturnRepository;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.BuyerCommercialStatus;
import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;

import java.util.List;

/**
 * BuyerManagementService
 *
 * Implements the Buyer Management services (per SDD - Services):
 * Register Buyer and Manage Buyer (consult, change commercial status,
 * retrieve orders/returns/refunds by permissions).
 */
public class BuyerManagementService implements RegisterBuyerUseCase, ManageBuyerUseCase {

    private final PersonRepository personRepository;
    private final BuyerRepository buyerRepository;
    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final NotificationService notificationService;

    public BuyerManagementService(PersonRepository personRepository,
                                  BuyerRepository buyerRepository,
                                  OrderRepository orderRepository,
                                  ReturnRepository returnRepository,
                                  NotificationService notificationService) {
        if (personRepository == null || buyerRepository == null || orderRepository == null
                || returnRepository == null || notificationService == null) {
            throw new IllegalArgumentException("BuyerManagementService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.buyerRepository = buyerRepository;
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Buyer registerBuyer(String identifier, String fullName, String email, Address primaryAddress) {
        requireText(identifier, "buyer identifier");
        requireText(fullName, "buyer full name");
        requireText(email, "buyer email");
        if (primaryAddress == null) {
            throw new IllegalArgumentException("A buyer requires an initial delivery address");
        }
        if (personRepository.existsByIdentifier(identifier)) {
            throw new DuplicateEmailException(identifier);
        }
        if (personRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        Buyer buyer = new Buyer(identifier, fullName, email, primaryAddress,
                BuyerCommercialStatus.ACTIVE, UserStatus.ACTIVE);
        buyerRepository.save(buyer);
        notificationService.notify(buyer, "Welcome to NexusMarket",
                "Your buyer account was created successfully");
        return buyer;
    }

    @Override
    public Buyer consultBuyer(String requesterId, String buyerId) {
        requireText(requesterId, "requester id");
        requireText(buyerId, "buyer id");
        requireConsultationAccess(requesterId, buyerId);
        return requireBuyer(buyerId);
    }

    @Override
    public Buyer changeCommercialStatus(String performerId, String buyerId, String statusCode) {
        requireText(performerId, "performer id");
        requireText(buyerId, "buyer id");
        requireText(statusCode, "commercial status code");

        Person performer = personRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrator '" + performerId + "' does not exist"));
        performer.requireActive();
        performer.requireRole("change a buyer commercial status", SystemRole.ADMINISTRATOR);

        Buyer buyer = requireBuyer(buyerId);
        buyer.setCommercialStatus(BuyerCommercialStatus.fromCode(statusCode));
        buyerRepository.save(buyer);
        notificationService.notify(buyer, "Commercial status updated",
                "Your commercial status is now " + buyer.getCommercialStatus().getCode());
        return buyer;
    }

    @Override
    public BuyerActivity consultBuyerActivity(String requesterId, String buyerId) {
        requireText(requesterId, "requester id");
        requireText(buyerId, "buyer id");
        requireConsultationAccess(requesterId, buyerId);
        Buyer buyer = requireBuyer(buyerId);
        List<Order> orders = orderRepository.findByBuyerId(buyerId);
        List<Return> returns = returnRepository.findByBuyerId(buyerId);
        return new BuyerActivity(buyer, orders, returns);
    }

    private void requireConsultationAccess(String requesterId, String buyerId) {
        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();
        if (!requester.getIdentifier().equals(buyerId)
                && requester.getRole() != SystemRole.ADMINISTRATOR
                && requester.getRole() != SystemRole.SUPERVISOR) {
            throw new InvalidRoleAssignmentException("consult buyer information",
                    requester.getRole(),
                    "an ADMINISTRATOR, a SUPERVISOR or the buyer itself");
        }
    }

    private Buyer requireBuyer(String buyerId) {
        return buyerRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Buyer '" + buyerId + "' does not exist"));
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
