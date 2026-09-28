package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.Refund;
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
import Application.domain.valueobjects.UserStatus;

import java.util.List;
import java.util.Objects;

/**
 * BuyerManagementService
 *
 * Implements RegisterBuyerUseCase and ManageBuyerUseCase. A buyer may
 * register itself or be registered by an Administrator, and enters the
 * platform with an ACTIVE commercial status. A buyer only accesses its
 * own information, orders, returns and refunds; only an Administrator
 * changes the commercial status.
 */
public class BuyerManagementService implements RegisterBuyerUseCase, ManageBuyerUseCase {

    private final BuyerRepository buyerRepository;
    private final PersonRepository personRepository;
    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public BuyerManagementService(BuyerRepository buyerRepository,
                                  PersonRepository personRepository,
                                  OrderRepository orderRepository,
                                  ReturnRepository returnRepository,
                                  AuthorizationService authorizationService,
                                  NotificationService notificationService) {
        if (buyerRepository == null || personRepository == null || orderRepository == null
                || returnRepository == null || authorizationService == null || notificationService == null) {
            throw new IllegalArgumentException("BuyerManagementService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.personRepository = personRepository;
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Override
    public Buyer registerBuyer(String performerId, String identifier, String fullName, String email,
                               Address primaryAddress) {
        ServiceValidations.requireText(identifier, "buyer identifier");
        ServiceValidations.requireText(fullName, "buyer full name");
        ServiceValidations.requireText(email, "buyer email");
        if (primaryAddress == null) {
            throw new IllegalArgumentException("A buyer requires a primary delivery address");
        }
        if (performerId != null && !performerId.isBlank()) {
            authorizationService.requirePermission(performerId, BusinessOperation.REGISTER_BUYER);
        }
        ServiceValidations.requireUniqueIdentity(personRepository, identifier, email);

        Buyer buyer = new Buyer(identifier, fullName, email, primaryAddress,
                BuyerCommercialStatus.ACTIVE, UserStatus.ACTIVE);
        buyerRepository.save(buyer);
        notificationService.notify(buyer, "Welcome to NexusMarket",
                "Your buyer account is ready to place orders");
        return buyer;
    }

    @Override
    public Buyer consultBuyer(String requesterId, String buyerId) {
        return findAccessibleBuyer(requesterId, buyerId, BusinessOperation.CONSULT_BUYER);
    }

    @Override
    public Buyer updateBuyer(String requesterId, String buyerId, String fullName, String email,
                             Address primaryAddress, List<Address> additionalAddresses) {
        Buyer buyer = findAccessibleBuyer(requesterId, buyerId, BusinessOperation.UPDATE_BUYER);
        ServiceValidations.requireEmailAvailableFor(personRepository, email, buyer.getIdentifier());

        buyer.updateContactInformation(fullName, email);
        if (primaryAddress != null) {
            buyer.setPrimaryAddress(primaryAddress);
        }
        if (additionalAddresses != null) {
            buyer.replaceAdditionalAddresses(additionalAddresses);
        }
        buyerRepository.save(buyer);
        notificationService.notify(buyer, "Buyer updated", "Your buyer information was updated");
        return buyer;
    }

    @Override
    public Buyer changeCommercialStatus(String requesterId, String buyerId, BuyerCommercialStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Commercial status must not be null");
        }
        authorizationService.requirePermission(requesterId, BusinessOperation.CHANGE_BUYER_COMMERCIAL_STATUS);
        Buyer buyer = findBuyer(buyerId);
        buyer.setCommercialStatus(status);
        buyerRepository.save(buyer);
        notificationService.notify(buyer, "Commercial status changed",
                "Your commercial status is now " + status.getName());
        return buyer;
    }

    @Override
    public List<Order> consultBuyerOrders(String requesterId, String buyerId) {
        Buyer buyer = findAccessibleBuyer(requesterId, buyerId, BusinessOperation.CONSULT_BUYER);
        return orderRepository.findByBuyerId(buyer.getIdentifier());
    }

    @Override
    public List<Return> consultBuyerReturns(String requesterId, String buyerId) {
        Buyer buyer = findAccessibleBuyer(requesterId, buyerId, BusinessOperation.CONSULT_BUYER);
        return returnRepository.findByBuyerId(buyer.getIdentifier());
    }

    @Override
    public List<Refund> consultBuyerRefunds(String requesterId, String buyerId) {
        Buyer buyer = findAccessibleBuyer(requesterId, buyerId, BusinessOperation.CONSULT_BUYER);
        return returnRepository.findByBuyerId(buyer.getIdentifier()).stream()
                .map(Return::getRefund)
                .filter(Objects::nonNull)
                .toList();
    }

    private Buyer findAccessibleBuyer(String requesterId, String buyerId, BusinessOperation operation) {
        Person requester = authorizationService.requirePermission(requesterId, operation);
        Buyer buyer = findBuyer(buyerId);
        authorizationService.requireBuyerAccess(requester, buyer);
        return buyer;
    }

    private Buyer findBuyer(String buyerId) {
        ServiceValidations.requireText(buyerId, "buyer id");
        return buyerRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer '" + buyerId + "' does not exist"));
    }
}
