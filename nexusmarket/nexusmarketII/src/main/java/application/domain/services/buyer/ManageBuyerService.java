package application.domain.services.buyer;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.ports.out.BuyerRepository;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.PersonRepository;
import application.domain.ports.out.ReturnRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;

import java.util.List;
import java.util.Objects;

/**
 * ManageBuyerService
 *
 * Catalog service "Manage Buyer". A buyer only
 * accesses its own information, orders, returns and refunds; only an
 * Administrator changes the commercial status.
 */
public class ManageBuyerService {

    private final BuyerRepository buyerRepository;
    private final PersonRepository personRepository;
    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;
    private final NotificationService notificationService;

    public ManageBuyerService(BuyerRepository buyerRepository,
                              PersonRepository personRepository,
                              OrderRepository orderRepository,
                              ReturnRepository returnRepository,
                              ValidatePermissionsService permissionsService,
                              ValidateOwnershipAccessService ownershipService,
                              NotificationService notificationService) {
        if (buyerRepository == null || personRepository == null || orderRepository == null
                || returnRepository == null || permissionsService == null || ownershipService == null
                || notificationService == null) {
            throw new IllegalArgumentException("ManageBuyerService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.personRepository = personRepository;
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
        this.notificationService = notificationService;
    }

    public Buyer consultBuyer(String requesterId, String buyerId) {
        return findAccessibleBuyer(requesterId, buyerId, BusinessOperation.CONSULT_BUYER);
    }

    /**
     * @param additionalAddresses replaces the additional addresses (null keeps them)
     */
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

    public Buyer changeCommercialStatus(String requesterId, String buyerId, BuyerCommercialStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Commercial status must not be null");
        }
        permissionsService.requirePermission(requesterId, BusinessOperation.CHANGE_BUYER_COMMERCIAL_STATUS);
        Buyer buyer = findBuyer(buyerId);
        buyer.setCommercialStatus(status);
        buyerRepository.save(buyer);
        notificationService.notify(buyer, "Commercial status changed",
                "Your commercial status is now " + status.getName());
        return buyer;
    }

    public List<Order> consultBuyerOrders(String requesterId, String buyerId) {
        Buyer buyer = findAccessibleBuyer(requesterId, buyerId, BusinessOperation.CONSULT_BUYER);
        return orderRepository.findByBuyerId(buyer.getIdentifier());
    }

    public List<Return> consultBuyerReturns(String requesterId, String buyerId) {
        Buyer buyer = findAccessibleBuyer(requesterId, buyerId, BusinessOperation.CONSULT_BUYER);
        return returnRepository.findByBuyerId(buyer.getIdentifier());
    }

    public List<Refund> consultBuyerRefunds(String requesterId, String buyerId) {
        Buyer buyer = findAccessibleBuyer(requesterId, buyerId, BusinessOperation.CONSULT_BUYER);
        return returnRepository.findByBuyerId(buyer.getIdentifier()).stream()
                .map(Return::getRefund)
                .filter(Objects::nonNull)
                .toList();
    }

    private Buyer findAccessibleBuyer(String requesterId, String buyerId, BusinessOperation operation) {
        Person requester = permissionsService.requirePermission(requesterId, operation);
        Buyer buyer = findBuyer(buyerId);
        ownershipService.requireBuyerAccess(requester, buyer);
        return buyer;
    }

    private Buyer findBuyer(String buyerId) {
        ServiceValidations.requireText(buyerId, "buyer id");
        return buyerRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
    }
}
